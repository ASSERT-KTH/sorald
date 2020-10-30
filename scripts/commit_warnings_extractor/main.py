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
import pandas as pd

from typing import Tuple, List, Mapping


SORALD_JAR_PATH = (
    pathlib.Path(__file__).absolute().parent.parent.parent
    / "target"
    / "sorald-1.1-SNAPSHOT-jar-with-dependencies.jar"
).resolve(strict=True)

REPOS = (
    # "https://github.com/inria/spoon",
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
        frame = pd.DataFrame.from_dict(warning_stats)
        raw_data_dst = WARNING_STATS_OUTPUT_DIR / repo_url.replace("/", "_")
        raw_data_dst.write_text(frame.to_csv())

    print(f"Results written to {output_dir}")


def compute_deltas(stats_per_commit: Mapping[str, Mapping[str, int]]) -> pd.DataFrame:
    """Compute deltas between the commits for each warning, in order. A
    negative delta indicates removal of warnings, and positive delta indicates
    warnings added with the commit. In other words, a negative delta is good,
    a positive delta is bad, and a 0-delta is neither.
    """
    frame = pd.DataFrame.from_dict(stats_per_commit)


def extract_warning_stats_from_remote_repo(
    repo_url: str,
) -> Mapping[str, Mapping[str, int]]:
    with tempfile.TemporaryDirectory() as tmpdir:
        workdir = pathlib.Path(tmpdir)
        repo_root = workdir / pathlib.Path(repo_url).stem
        repo = git.Repo.clone_from(repo_url, to_path=repo_root)
        commits = [commit.hexsha for commit in repo.iter_commits()]
        return extract_warnings_stats_from_local_repo(repo_root, commits)


def extract_warnings_stats_from_local_repo(
    repo_root: pathlib.Path, commits: List[str]
) -> Mapping[str, Mapping[str, int]]:
    # assuming 2 threads per core
    num_cpus = multiprocessing.cpu_count() // 4
    with multiprocessing.Pool(num_cpus) as pool:
        extract = functools.partial(extract_commit_warning_stats, repo_root=repo_root)
        warnings_extraction_iter = pool.imap(extract, commits)

        warnings_extraction_progress = tqdm(
            warnings_extraction_iter,
            total=len(commits),
            unit="commit",
            desc=f"Processing {repo_root.name}",
        )

        # reverse such that most recent commit ends up last in collection
        results = reversed(list(warnings_extraction_progress))
        return {hexsha: stats_dict for hexsha, stats_dict in results if stats_dict}


def extract_commit_warning_stats(
    commit_sha: str, repo_root: pathlib.Path
) -> Tuple[str, Mapping[str, int]]:
    return commit_sha, _extract_commit_warning_stats(commit_sha, repo_root)


def _extract_commit_warning_stats(
    commit_sha: str, repo_root: pathlib.Path
) -> Mapping[str, int]:
    with temporary_checkout(repo_root, commit_sha) as checkout_path:
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

    stat_lines = sorted(
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
def temporary_checkout(repo_root: pathlib.Path, ref: str):
    repo = git.Repo(repo_root)
    with tempfile.TemporaryDirectory() as tmpdir:
        workdir = pathlib.Path(tmpdir)
        path_hash = hashlib.sha1(str(workdir).encode("utf8")).hexdigest()
        repo_copy_dir = workdir / path_hash

        try:
            repo.git.worktree("add", str(repo_copy_dir), ref)
            yield repo_copy_dir
        finally:
            repo.git.worktree("remove", str(repo_copy_dir))

if __name__ == "__main__":
    main()
