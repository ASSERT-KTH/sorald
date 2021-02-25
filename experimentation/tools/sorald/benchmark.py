"""Script for benchmarking Sorald's performance on a set of repositories."""

import argparse
import sys
import pathlib
import git
import tempfile
import json
import dataclasses

from multiprocessing import Pool, Queue
from typing import List, Mapping, Iterable, Tuple

import pandas as pd
import tqdm

from sorald._helpers import soraldwrapper, jsonkeys

SORALD_JAR = "/home/slarse/Documents/github/work/sorald/target/"

STATS_COLUMNS = [
    "url",
    "commit",
    "rule_key",
    "total_repair_ratio",
    "attempted_repair_ratio",
]


def main(args: List[str]):
    parser = argparse.ArgumentParser(
        prog=f"{__package__}.{pathlib.Path(__file__).name[:-3]}"
    )
    parser.add_argument(
        "--commits-csv",
        help="path to a csv file with commits, at least containing the column "
        "headers 'url' and 'commit'",
        required=True,
        type=pathlib.Path,
    )
    parser.add_argument(
        "-o",
        "--output",
        help="path to the output CSV file",
        required=True,
        type=pathlib.Path,
    )
    parser.add_argument(
        "-c",
        "--compare",
        help="use the input data to compare with the benchmark results. "
        f"Requires all column headers the commits file: {STATS_COLUMNS}",
        action="store_true",
    )
    parser.add_argument(
        "-p",
        "--parallel-experiments",
        help="amount of experiments to run in parallel",
        type=int,
        default=1,
    )

    parsed_args = parser.parse_args(args)

    commits_frame = pd.read_csv(parsed_args.commits_csv)

    input_columns = list(commits_frame.columns.array)
    if parsed_args.compare and input_columns != STATS_COLUMNS:
        raise RuntimeError(
            f"Cannot compare with input data, expected columns {STATS_COLUMNS} "
            f"but found {input_columns}"
        )

    rule_keys = ["1444", "1854", "1948", "2116", "2142"]
    results = benchmark_commits(
        commits_frame, rule_keys, parsed_args.parallel_experiments
    )

    # convert to dataframe
    results_frame = pd.DataFrame(columns=STATS_COLUMNS)

    for commit_stats in results:
        for rs in commit_stats.repair_stats:
            results_frame.loc[len(results_frame)] = (
                commit_stats.project_url,
                commit_stats.commit_sha,
                rs.rule_key,
                rs.total_repair_ratio,
                rs.attempted_repair_ratio,
            )

    results_frame.to_csv(parsed_args.output, index=False)


def benchmark_commits(
    commits_frame: pd.DataFrame, rule_keys: List[str], num_parallel_experiments: int
) -> Iterable["CommitRepairStats"]:
    pool = Pool(num_parallel_experiments)
    args = [(row.url, row.commit, rule_keys) for _, row in commits_frame.iterrows()]
    results = pool.imap(imappable_benchmark_commit, args)
    results_progress = tqdm.tqdm(
        results, desc="Processing commits", total=len(commits_frame)
    )

    for result in results_progress:
        results_progress.write(f"Processed {result.commit_id}")
        yield result


def imappable_benchmark_commit(tup: Tuple[str, str, List[str]]) -> "CommitRepairStats":
    return benchmark_commit(*tup)


def benchmark_commit(
    url: str, commit: str, rule_keys: List[str]
) -> "CommitRepairStats":
    with tempfile.TemporaryDirectory() as tmpdir:
        workdir = pathlib.Path(tmpdir)
        repo = git.Repo.clone_from(url, to_path=workdir)
        repo.git.checkout(commit)
        repo_repair_stats = _benchmark_commit(repo, rule_keys)

    return repo_repair_stats


def _benchmark_commit(repo: git.Repo, rule_keys: List[str]) -> "CommitRepairStats":
    workdir = pathlib.Path(repo.working_dir)
    stats_file = workdir / "stats.json"
    proc = soraldwrapper.sorald(
        "repair",
        original_files_path=pathlib.Path(repo.working_dir),
        stats_output_file=workdir / stats_file,
        file_output_strategy="IN_PLACE",
        rule_keys=rule_keys,
    )
    if proc.returncode != 0:
        print(proc.stderr.decode(sys.getdefaultencoding()), file=sys.stderr)
        raise RuntimeError("failed to execute Sorald")

    stats = json.loads(stats_file.read_text(encoding="utf8"))
    repair_stats = map(
        RepairStats.from_repair_dict, stats[jsonkeys.SORALD_STATS.REPAIRS]
    )

    repairs_dict = {rs.rule_key: rs for rs in repair_stats}
    for key in set(rule_keys) - repairs_dict.keys():
        repairs_dict[key] = RepairStats(*([0] * len(dataclasses.fields(RepairStats))))

    return CommitRepairStats(
        project_url=next(repo.remote().urls),
        commit_sha=repo.head.commit.hexsha,
        repair_stats=list(repairs_dict.values()),
    )


@dataclasses.dataclass(frozen=True)
class RepairStats:
    """Statistics for a single repair."""

    rule_key: str
    num_violations_before: int
    num_violations_after: int
    num_performed_repairs: int
    num_crashed_repairs: int

    @property
    def num_successful_repairs(self) -> int:
        return self.num_violations_before - self.num_violations_after

    @property
    def num_failed_repairs(self) -> int:
        return self.num_performed_repairs - self.num_successful_repairs

    @property
    def total_repair_ratio(self) -> float:
        return (
            self.num_successful_repairs / self.num_violations_before
            if self.num_violations_before > 0
            else 1
        )

    @property
    def attempted_repair_ratio(self) -> float:
        return (
            self.num_successful_repairs / self.num_performed_repairs
            if self.num_performed_repairs > 0
            else 1
        )

    @staticmethod
    def from_repair_dict(repair: dict) -> "RepairStats":
        stat_keys = jsonkeys.SORALD_STATS
        return RepairStats(
            rule_key=repair[stat_keys.RULE_KEY],
            num_violations_before=repair[stat_keys.VIOLATIONS_BEFORE],
            num_violations_after=repair[stat_keys.VIOLATIONS_AFTER],
            num_performed_repairs=repair[stat_keys.NUM_PERFORMED_REPAIRS],
            num_crashed_repairs=repair[stat_keys.NUM_CRASHED_REPAIRS],
        )

    def __add__(self, other: "RepairStats") -> "RepairStats":
        if other.rule_key != self.rule_key:
            raise ValueError(f"rule key mismatch {self.rule_key} and {other.rule_key}")
        self_dict = dataclasses.asdict(self)
        other_dict = dataclasses.asdict(other)
        additive_dict = {
            key: value + other_value
            for key, value in self_dict.items()
            if isinstance(other_value := other_dict[key], int)
        }
        return RepairStats(rule_key=self.rule_key, **additive_dict)


@dataclasses.dataclass(frozen=True)
class CommitRepairStats:
    project_url: str
    commit_sha: str
    repair_stats: List[RepairStats]

    @property
    def commit_id(self):
        return f"{self.project_url}@{self.commit_sha}"


if __name__ == "__main__":
    main(sys.argv[1:])
