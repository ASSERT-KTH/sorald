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
import itertools

import git
from tqdm import tqdm
import pandas as pd

from typing import Tuple, List, Mapping, Iterable


SORALD_JAR_PATH = (
    pathlib.Path(__file__).absolute().parent.parent.parent
    / "target"
    / "sorald-1.1-SNAPSHOT-jar-with-dependencies.jar"
).resolve(strict=True)

REPOS = [
    f"https://github.com/{repo}"
    for repo in (
        "ontop/ontop",
        "opendatalab-de/geojson-jackson",
        "apache/commons-imaging",
        "SpigotMC/BungeeCord",
        "Pardot/Rhombus",
        "kpelykh/docker-java",
        "stanfordnlp/CoreNLP",
        "NGDATA/hbase-indexer",
        "spotify/hdfs2cass",
        "octo-technology/sonar-objective-c",
        "ppat/storm-rabbitmq",
        "dkunzler/esperandro",
        "ParallelAI/SpyGlass",
        "FellowTraveler/otapij",
        "gwtd3/gwt-d3",
        "OpenHFT/Java-Lang",
        "rcarz/jira-client",
        "jitsi/jitsi-videobridge",
        "RisingOak/jenkins-client",
        "Beh01der/EasyFlow",
        "jitsi/libjitsi",
        "videlalvaro/clochure",
        "lookfirst/sardine",
        "rackerlabs/atom-hopper",
        "Esri/geometry-api-java",
        "rschreijer/lutung",
        "eXist-db/exist",
        "joel-costigliola/assertj-core",
        "alibaba/druid",
        "alibaba/fastjson",
    )
]

WARNING_STATS_OUTPUT_DIR = pathlib.Path(__file__).parent / "warning_stats_output"

NUM_COMMITS_PER_REPO = 20
COMMIT_STEP_SIZE = 20


def main():
    extract_warnings(
        repo_urls=REPOS,
        output_dir=WARNING_STATS_OUTPUT_DIR,
        num_commits_per_repo=NUM_COMMITS_PER_REPO,
        commit_step_size=COMMIT_STEP_SIZE,
    )


def extract_warnings(
    repo_urls: List[str],
    output_dir: pathlib.Path,
    num_commits_per_repo: int,
    commit_step_size: int,
) -> None:
    repo_url_iter = tqdm(repo_urls, desc="Overall progress", unit="repo")
    output_dir.mkdir(exist_ok=True, parents=True)

    for repo_url in repo_url_iter:
        warning_stats = extract_warning_stats_from_remote_repo(
            repo_url,
            num_commits=num_commits_per_repo,
            commit_step_size=commit_step_size,
        )
        frame = pd.DataFrame.from_dict(warning_stats)
        raw_data_dst = WARNING_STATS_OUTPUT_DIR / (
            repo_url.replace("/", "_").replace(":", "_") + ".csv"
        )
        raw_data_dst.write_text(frame.to_csv())

        deltas_dst = WARNING_STATS_OUTPUT_DIR / (raw_data_dst.stem + ".deltas.csv")
        deltas_dst.write_text(frame.diff(axis=1).fillna(frame.iloc[0]).to_csv())

    print(f"Results written to {output_dir}")


def extract_warning_stats_from_remote_repo(
    repo_url: str, num_commits: int, commit_step_size: int
) -> Mapping[str, Mapping[str, int]]:
    with tempfile.TemporaryDirectory() as tmpdir:
        workdir = pathlib.Path(tmpdir)
        repo_root = workdir / pathlib.Path(repo_url).stem
        repo = git.Repo.clone_from(repo_url, to_path=repo_root)
        commits = [commit.hexsha for commit in repo.iter_commits()][::commit_step_size][
            :num_commits
        ]
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
    all_warnings = collections.defaultdict(int)
    with temporary_checkout(repo_root, commit_sha) as checkout_path:
        for src_main in find_main_sources(checkout_path):
            warnings = extract_warning_stats_from_dir(src_main)
            for key, value in warnings.items():
                all_warnings[key] += value

    return all_warnings


def extract_warning_stats_from_dir(root_path: pathlib.Path) -> Mapping[str, int]:
    proc = subprocess.run(
        [
            "java",
            "-cp",
            str(SORALD_JAR_PATH),
            "sorald.miner.MineSonarWarnings",
            "--originalFilesPath",
            str(root_path),
            "--ruleTypes",
            "vulnerability",
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
    return files


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
