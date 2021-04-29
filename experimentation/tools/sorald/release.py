"""Script for creating a release of Sorald."""

import argparse
import os
import pathlib
import shlex
import subprocess
import sys
import time
import secrets

from typing import List, Optional

import github
import requests

REPO_SLUG = "SpoonLabs/sorald"
REPO_SSH_URL = f"git@github.com:{REPO_SLUG}.git"
REPO_MAIN_BRANCH = "master"
RELEASE_BRANCH_TEMPLATE = "release/{}"

GITHUB_API_BASE_URL = "https://api.github.com"

GITHUB_TOKEN_ENV = "GITHUB_TOKEN"
REQUIRED_TOKEN_SCOPES = {"repo"}


def main() -> None:
    parsed_args = _parse_args(sys.argv[1:])
    gh_token = os.getenv(GITHUB_TOKEN_ENV)
    if not gh_token:
        raise RuntimeError(
            f"Please set a GitHub token in the '{GITHUB_TOKEN_ENV}' environment variable"
        )
    _check_token_scopes(gh_token)

    _prepare_release(parsed_args.release_version)
    _push_release()
    _create_github_release(gh_token)


def _parse_args(args: List[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        prog="sorald.release",
        description="Script for creating a release of "
        "Sorald. By default, a release is made with the version number in the "
        "POM, with SNAPSHOT removed. A new development iteration is prepared in "
        "a separate commit, with a bump on the patch version number.",
    )
    parser.add_argument(
        "--release-version",
        help="version number to use for the release "
        "(defaults to the current version without SNAPSHOT)",
    )
    return parser.parse_args(args)


def _prepare_release(release_version: Optional[str]) -> None:
    tmp_branch_name = secrets.token_hex(10)
    _run_cmd(f"git checkout -b {tmp_branch_name}")

    release_version_arg = (
        f"-DreleaseVersion={release_version}" if release_version is not None else ""
    )
    _run_cmd(f"mvn -B clean release:prepare -DpushChanges=false {release_version_arg}")
    _sign_last_n_commits(2)
    _sign_release_tag()

    release_tag = _get_release_tag_name()
    _run_cmd(f"git checkout -b {RELEASE_BRANCH_TEMPLATE.format(release_tag)}")
    _run_cmd(f"git branch -D {tmp_branch_name}")


def _push_release() -> None:
    release_tag = _get_release_tag_name()
    _run_cmd(
        f"git push -u {REPO_SSH_URL} {RELEASE_BRANCH_TEMPLATE.format(release_tag)} --tags"
    )


def _push_current_branch() -> None:
    _run_cmd(f"git push")


def _sign_last_n_commits(n: int) -> None:
    _run_cmd(f"git rebase -x 'git commit -S --amend --no-edit' HEAD~{n}")


def _sign_release_tag() -> None:
    tag_name = _get_release_tag_name()
    _run_cmd(f"git tag -s -f {tag_name} -m 'Release {tag_name}' HEAD~")


def _get_release_tag_name() -> str:
    for line in open("release.properties", mode="r", encoding=sys.getdefaultencoding()):
        if line.startswith("scm.tag="):
            _, tag_name = line.split("=")
            return tag_name.strip()
    raise RuntimeError("expected to find 'scm.tag' in release.properties")


def _check_token_scopes(token: str) -> None:
    gh = github.Github(login_or_token=token, base_url=GITHUB_API_BASE_URL)
    gh.get_rate_limit()  # make any get request to initialize the API wrapper
    if not REQUIRED_TOKEN_SCOPES.issubset(gh.oauth_scopes or {}):
        raise RuntimeError(f"access token must have scopes {REQUIRED_TOKEN_SCOPES}")


def _create_github_release(token: str):
    gh = github.Github(login_or_token=token, base_url=GITHUB_API_BASE_URL)
    repo = gh.get_repo("SpoonLabs/sorald")
    tag_name = _get_release_tag_name()
    release = repo.create_git_release(
        tag=tag_name, name=tag_name, message=f"Release {tag_name}"
    )

    # import this here as we need sorald to be packaged before importing
    import sorald._helpers.soraldwrapper

    release.upload_asset(
        str(sorald._helpers.soraldwrapper.DEFAULT_SORALD_JAR_PATH),
        content_type="application/zip",
        name=sorald._helpers.soraldwrapper.DEFAULT_SORALD_JAR_PATH.name,
    )


def _run_cmd(cmd) -> subprocess.CompletedProcess:
    proc = subprocess.run(shlex.split(cmd))
    proc.check_returncode()
    return proc


if __name__ == "__main__":
    main()
