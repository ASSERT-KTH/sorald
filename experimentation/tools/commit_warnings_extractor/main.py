"""Script for running the warnings miner on all commits on the default branch
of remote repositories.
"""

import argparse
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
import itertools
import dataclasses
import re

import git
from tqdm import tqdm
import pandas as pd

from typing import Tuple, List, Mapping, Iterable, Optional


SORALD_JAR_PATH = (
    pathlib.Path(__file__).absolute().parent.parent.parent.parent
    / "target"
    / "sorald-1.1-SNAPSHOT-jar-with-dependencies.jar"
).resolve(strict=False)

WARNING_STATS_OUTPUT_DIR = (
    pathlib.Path(__file__).parent / "warning_stats_output"
).resolve(strict=False)


NUM_COMMITS_PER_REPO = 20
COMMIT_STEP_SIZE = 20


@dataclasses.dataclass
class Config:
    output_dir: pathlib.Path
    num_commits_per_repo: int
    commit_step_size: int
    num_cpus: int
    sorald_jar: pathlib.Path
    miner_options: List[str]


@dataclasses.dataclass(frozen=True)
class MinerOption:
    name: str
    value: str

    @staticmethod
    def parse(raw_miner_option: str) -> "MinerOption":
        match = re.match("(.*?)=(.*)", raw_miner_option)
        if not match:
            raise ValueError(f"invalid miner option: {raw_miner_option}")
        return MinerOption(*match.groups())


def main():
    parsed = parse_args(sys.argv[1:])
    repo_urls = parse_repo_urls(parsed.repo_list)
    config = Config(
        output_dir=parsed.output_dir.resolve(strict=False),
        num_commits_per_repo=parsed.num_commits_per_repo,
        commit_step_size=parsed.step_size,
        num_cpus=parsed.num_cpus,
        sorald_jar=parsed.sorald_jar,
        miner_options=parse_miner_options(parsed.miner_option),
    )
    extract_warnings(repo_urls=repo_urls, config=config)


def parse_args(args: List[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Script for mining Sonar warnings on a per-commit basis, "
        "using Sorald's warnings miner.",
        formatter_class=argparse.ArgumentDefaultsHelpFormatter,
    )

    parser.add_argument(
        help="path to a file with names of GitHub repos, with one repo per line on "
        "the form <OWNER_NAME>/<REPO_NAME> (e.g. spoonlabs/sorald)",
        dest="repo_list",
        type=pathlib.Path,
    )
    parser.add_argument(
        "-o",
        "--output-dir",
        help="path to the output directory",
        type=pathlib.Path,
        default=WARNING_STATS_OUTPUT_DIR,
    )
    parser.add_argument(
        "-n",
        "--num-commits-per-repo",
        help="total amount of commits to sample from a repository, starting with the latest",
        type=int,
        default=sys.maxsize,
    )
    parser.add_argument(
        "-s",
        "--step-size",
        help="how large a step to take when sampling commits (e.g. step size 1 means "
        "sample every commit, step size of 3 means sample commits N, N-3, N-6 ..., "
        "where N is the total amount of commits)",
        type=int,
        default=1,
    )
    parser.add_argument(
        "--sorald-jar",
        help="path to the Sorald jarfile with dependencies",
        type=pathlib.Path,
        default=SORALD_JAR_PATH,
    )
    parser.add_argument(
        "--num-cpus",
        help="amount of CPUs to use for processing",
        type=int,
        default=max(multiprocessing.cpu_count() // 4, 1),
    )
    parser.add_argument(
        "--mo",
        "--miner-option",
        dest="miner_option",
        help="option to pass directly to the miner, without leading `--`. "
        "Can be repeated for more options. "
        "Example: --miner-option 'ruleTypes=vulnerability'",
        action="append",
    )
    parsed_args = parser.parse_args(args)

    if not parsed_args.sorald_jar.exists():
        print(f"no such file: {parsed_args.sorald_jar}", file=sys.stderr)
        print(
            f"please specify path to Sorald jarfile with --sorald-jar", file=sys.stderr
        )
        parser.print_usage()
        sys.exit(1)

    return parsed_args


def parse_repo_urls(repos_list: pathlib.Path) -> List[str]:
    return [
        f"https://github.com/{repo}"
        for raw_repo in repos_list.read_text(encoding=sys.getdefaultencoding()).split(
            "\n"
        )
        if (repo := raw_repo.strip())
    ]


def parse_miner_options(raw_miner_options: Optional[List[str]]) -> List[MinerOption]:
    return list(map(MinerOption.parse, raw_miner_options or []))


def extract_warnings(repo_urls: List[str], config: Config) -> None:
    repo_url_iter = tqdm(repo_urls, desc="Overall progress", unit="repo")
    config.output_dir.mkdir(exist_ok=True, parents=True)

    for repo_url in repo_url_iter:
        warning_stats = extract_warning_stats_from_remote_repo(
            repo_url,
            config=config,
        )
        frame = pd.DataFrame.from_dict(warning_stats)
        raw_data_dst = config.output_dir / (
            repo_url.replace("/", "_").replace(":", "_") + ".csv"
        )
        raw_data_dst.write_text(frame.to_csv())

        deltas_dst = config.output_dir / (raw_data_dst.stem + ".deltas.csv")
        deltas_dst.write_text(frame.diff(axis=1).fillna(frame.iloc[0]).to_csv())

    print(f"Results written to {config.output_dir}")


def extract_warning_stats_from_remote_repo(
    repo_url: str,
    config: Config,
) -> Mapping[str, Mapping[str, int]]:
    with tempfile.TemporaryDirectory() as tmpdir:
        workdir = pathlib.Path(tmpdir)
        repo_root = workdir / pathlib.Path(repo_url).stem
        repo = git.Repo.clone_from(repo_url, to_path=repo_root)
        commits = [commit.hexsha for commit in repo.iter_commits()][
            :: config.commit_step_size
        ][: config.num_commits_per_repo]
        return extract_warnings_stats_from_local_repo(repo_root, commits, config)


def extract_warnings_stats_from_local_repo(
    repo_root: pathlib.Path,
    commits: List[str],
    config: Config,
) -> Mapping[str, Mapping[str, int]]:
    with multiprocessing.Pool(config.num_cpus) as pool:
        extract = functools.partial(
            extract_commit_warning_stats,
            repo_root=repo_root,
            config=config,
        )
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
    commit_sha: str, repo_root: pathlib.Path, config: Config
) -> Tuple[str, Mapping[str, int]]:
    return commit_sha, _extract_commit_warning_stats(commit_sha, repo_root, config)


def _extract_commit_warning_stats(
    commit_sha: str, repo_root: pathlib.Path, config: Config
) -> Mapping[str, int]:
    all_warnings = collections.defaultdict(int)
    with temporary_checkout(repo_root, commit_sha) as checkout_path:
        for src_main in find_main_sources(checkout_path):
            warnings = extract_warning_stats_from_dir(src_main, config)
            for key, value in warnings.items():
                all_warnings[key] += value

    return all_warnings


def extract_warning_stats_from_dir(
    root_path: pathlib.Path, config: Config
) -> Mapping[str, int]:
    extra_args = list(
        itertools.chain.from_iterable(
            (f"--{opt.name}", opt.value) for opt in config.miner_options
        )
    )
    proc = subprocess.run(
        [
            "java",
            "-jar",
            str(config.sorald_jar),
            "mine",
            "--originalFilesPath",
            str(root_path),
            *extra_args,
        ],
        capture_output=True,
    )

    if proc.returncode != 0:
        print(proc.stderr.decode(sys.getdefaultencoding()), file=sys.stderr)
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


def find_main_sources(project_root: pathlib.Path) -> List[pathlib.Path]:
    pom_files = project_root.rglob("pom.xml")
    files = list(
        itertools.chain.from_iterable(
            find_main_sources_relative_to_pom(pom_file)
            for pom_file in pom_files
            if "test"
            not in (parent_names := {parent.name for parent in pom_file.parents})
            and "tests" not in parent_names
        )
    )
    return files or [project_root]


def find_main_sources_relative_to_pom(pom_file: pathlib.Path) -> List[pathlib.Path]:
    pom_dir = pom_file.parent
    src_main = pom_dir / "src" / "main"
    if src_main.is_dir():
        return [src_main]

    # conventional source main wasn't there, try to find other source directories
    return [
        src
        for src in pom_dir.iterdir()
        if src.name not in {"test", "tests"} and src.is_dir()
    ]


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
