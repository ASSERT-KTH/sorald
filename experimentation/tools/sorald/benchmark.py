"""Script for benchmarking Sorald's performance on a set of repositories."""

import argparse
import sys
import pathlib
import git
import tempfile
import json
import dataclasses

from typing import List, Mapping, Iterable

from sorald._helpers import soraldwrapper, jsonkeys

SORALD_JAR = "/home/slarse/Documents/github/work/sorald/target/"


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
        return self.num_successful_repairs / self.num_violations_before

    @property
    def attempted_repair_ratio(self) -> float:
        return self.num_successful_repairs / self.num_performed_repairs

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


def main(args: List[str]):
    parser = argparse.ArgumentParser(
        prog=f"{__package__}.{pathlib.Path(__file__).name[:-3]}"
    )
    parser.add_argument("--repos-csv", required=True, type=pathlib.Path)

    parsed_args = parser.parse_args(args)

    with tempfile.TemporaryDirectory() as tmpdir:
        workdir = pathlib.Path(tmpdir)
        repo = git.Repo.clone_from(parsed_args.git_url, to_path=workdir)
        repo.git.checkout(parsed_args.commit_sha)

        repo_repair_stats = benchmark_repo(repo)
        from pprint import pprint

        pprint(repo_repair_stats)

        input()


def benchmark_repo(repo: git.Repo) -> Mapping[str, RepairStats]:
    workdir = pathlib.Path(repo.working_dir)
    stats_file = workdir / "stats.json"
    proc = soraldwrapper.sorald(
        "repair",
        original_files_path=pathlib.Path(repo.working_dir) / "src/main/java",
        stats_output_file=workdir / stats_file,
        file_output_strategy="IN_PLACE",
        rule_keys=soraldwrapper.available_rule_keys(),
    )
    if proc.returncode != 0:
        raise RuntimeError("failed to execute Sorald")

    stats = json.loads(stats_file.read_text(encoding="utf8"))
    repair_stats = map(
        RepairStats.from_repair_dict, stats[jsonkeys.SORALD_STATS.REPAIRS]
    )
    return {rs.rule_key: rs for rs in repair_stats}


if __name__ == "__main__":
    main(sys.argv[1:])
