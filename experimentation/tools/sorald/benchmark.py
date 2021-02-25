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


def main(args: List[str]):
    stats_columns = [
        "url",
        "commit",
        *[f.name for f in dataclasses.fields(RepairStats)],
    ]

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
        f"Requires all column headers the commits file: {stats_columns}",
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
    if parsed_args.compare and input_columns != stats_columns:
        raise RuntimeError(
            f"Cannot compare with input data, expected columns {stats_columns} "
            f"but found {input_columns}"
        )

    rule_keys = ["1444", "1854", "1948", "2116", "2142"]
    results = benchmark_commits(
        commits_frame, rule_keys, parsed_args.parallel_experiments
    )

    # convert to dataframe
    first_result = next(results)
    results_frame = pd.DataFrame(
        columns=stats_columns, data=first_result.to_results_tuples()
    )

    for commit_stats in results:
        for rs in commit_stats.to_results_tuples():
            results_frame.loc[len(results_frame)] = rs

    results_frame.sort_values(by=["url", "commit", "rule_key"])

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
        zeros = [0] * (len(dataclasses.fields(RepairStats)) - 1)
        repairs_dict[key] = RepairStats(key, *zeros)

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
    num_successful_repairs: int
    num_failed_repairs: int
    total_repair_ratio: float
    attempted_repair_ratio: float

    @staticmethod
    def from_repair_dict(repair: dict) -> "RepairStats":
        stat_keys = jsonkeys.SORALD_STATS

        num_violations_before = repair[stat_keys.VIOLATIONS_BEFORE]
        num_violations_after = repair[stat_keys.VIOLATIONS_AFTER]
        num_performed_repairs = repair[stat_keys.NUM_PERFORMED_REPAIRS]

        num_successful_repairs = num_violations_before - num_violations_after
        num_failed_repairs = num_performed_repairs - num_successful_repairs
        total_repair_ratio = (
            num_successful_repairs / num_violations_before
            if num_violations_before > 0
            else 0
        )
        attempted_repair_ratio = (
            num_successful_repairs / num_performed_repairs
            if num_performed_repairs > 0
            else 0
        )

        return RepairStats(
            rule_key=repair[stat_keys.RULE_KEY],
            num_violations_before=num_violations_before,
            num_violations_after=num_violations_after,
            num_performed_repairs=num_performed_repairs,
            num_crashed_repairs=repair[stat_keys.NUM_CRASHED_REPAIRS],
            num_successful_repairs=num_successful_repairs,
            num_failed_repairs=num_failed_repairs,
            total_repair_ratio=total_repair_ratio,
            attempted_repair_ratio=attempted_repair_ratio,
        )


@dataclasses.dataclass(frozen=True)
class CommitRepairStats:
    project_url: str
    commit_sha: str
    repair_stats: List[RepairStats]

    @property
    def commit_id(self):
        return f"{self.project_url}@{self.commit_sha}"

    def to_results_tuples(self) -> List[tuple]:
        return [
            (self.project_url, self.commit_sha, *dataclasses.astuple(rs))
            for rs in self.repair_stats
        ]


if __name__ == "__main__":
    main(sys.argv[1:])
