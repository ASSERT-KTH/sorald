"""Script for running the warnings miner on all commits on the default branch
of remote repositories.
"""

import sys
import subprocess
import pathlib
import tempfile
import contextlib
import functools
import hashlib
import multiprocessing
import collections
import csv

import git
from tqdm import tqdm

from typing import Tuple, List, Mapping


SORALD_JAR_PATH = (
    pathlib.Path(__file__).absolute().parent.parent.parent
    / "target"
    / "sorald-1.1-SNAPSHOT-jar-with-dependencies.jar"
).resolve(strict=True)

REPOS = (
    "https://github.com/inria/spoon",
    "https://github.com/mayurkadampro/Tic-Tac-Toe",
    "https://github.com/slarse/pkgextractor",
)

WARNING_STATS_OUTPUT_DIR = pathlib.Path(__file__).parent / "warning_stats_output"


def main():
    extract_warnings(repo_urls=REPOS, output_dir=WARNING_STATS_OUTPUT_DIR)


def extract_warnings(repo_urls: List[str], output_dir: pathlib.Path) -> None:
    repo_url_iter = tqdm(repo_urls, desc="Overall progress", unit="repo")
    output_dir.mkdir(exist_ok=True, parents=True)

    for repo_url in repo_url_iter:
        warning_stats = extract_warning_stats_from_remote_repo(repo_url)
        write_warning_stats(
            dst=WARNING_STATS_OUTPUT_DIR / repo_url.replace("/", "_"),
            stats_per_commit=warning_stats,
        )

    print(f"Results written to {output_dir}")


def extract_warning_stats_from_remote_repo(
    repo_url: str,
) -> Mapping[str, Mapping[str, int]]:
    with tempfile.TemporaryDirectory() as tmpdir:
        workdir = pathlib.Path(tmpdir)
        repo_root = workdir / "repo"
        repo = git.Repo.clone_from(repo_url, to_path=repo_root)
        commits = list(repo.iter_commits())
        return extract_warnings_stats_from_local_repo(repo, commits)


def extract_warnings_stats_from_local_repo(
    repo: git.Repo, commits: List[git.Commit]
) -> Mapping[str, Mapping[str, int]]:
    with multiprocessing.Pool(1) as pool:
        extract = functools.partial(extract_commit_warning_stats, repo=repo)
        warnings_extraction_iter = map(extract, commits)

        remote_url = next(repo.remote().urls)
        warnings_extraction_progress = tqdm(
            warnings_extraction_iter,
            total=len(commits),
            unit="commit",
            desc=f"Processing {remote_url}",
        )

        return {
            hexsha: stats_dict
            for hexsha, stats_dict in warnings_extraction_progress
            if stats_dict
        }


def extract_commit_warning_stats(
    commit: git.Commit, repo: git.Repo
) -> Tuple[str, Mapping[str, int]]:
    return commit.hexsha, _extract_commit_warning_stats(commit, repo)


def _extract_commit_warning_stats(
    commit: git.Commit, repo: git.Repo
) -> Mapping[str, int]:
    with temporary_checkout(repo, commit.hexsha) as checkout_path:
        proc = subprocess.run(
            [
                "java",
                "-cp",
                str(SORALD_JAR_PATH),
                "sorald.miner.MineSonarWarnings",
                "--originalFilesPath",
                str(find_source_main(checkout_path)),
            ],
            capture_output=True,
        )

    if proc.returncode != 0:
        return {}

    output = proc.stdout.decode(encoding=sys.getdefaultencoding())

    stat_lines = (
        stripped_line
        for line in output.split("\n")
        if (stripped_line := line.strip()) and not stripped_line.startswith("INFO ")
    )
    stats_dict = {
        t[0]: int(t[1]) for line in stat_lines if len((t := line.split("="))) == 2
    }
    return stats_dict


def find_source_main(project_root: pathlib.Path) -> pathlib.Path:
    conventional_source_main = project_root / "src" / "main"
    return (
        conventional_source_main if conventional_source_main.is_dir() else project_root
    )


@contextlib.contextmanager
def temporary_checkout(repo: git.Repo, ref: str):
    with tempfile.TemporaryDirectory() as tmpdir:
        workdir = pathlib.Path(tmpdir)
        path_hash = hashlib.sha1(str(workdir).encode("utf8")).hexdigest()
        repo_copy_dir = workdir / path_hash

        try:
            repo.git.worktree("add", str(repo_copy_dir), ref)
            yield repo_copy_dir
        finally:
            repo.git.worktree("remove", str(repo_copy_dir))


def write_warning_stats(
    dst: pathlib.Path, stats_per_commit: Mapping[str, Mapping[str, int]]
) -> None:
    if not stats_per_commit:
        raise ValueError("stats is empty")

    sorted_rule_names = sorted(list(stats_per_commit.values())[0].keys())
    with open(dst, mode="w", encoding="utf8") as file:
        writer = csv.writer(file)
        writer.writerow(["commit", *sorted_rule_names])
        for commit_sha, commit_stats in stats_per_commit.items():
            row_stats = [commit_stats[header] for header in sorted_rule_names]
            row = [commit_sha, *row_stats]
            writer.writerow(row)


if __name__ == "__main__":
    main()
