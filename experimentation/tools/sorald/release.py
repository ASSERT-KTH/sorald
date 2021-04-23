"""Script for creating a release of Sorald."""

import os
import pathlib
import shlex
import subprocess
import sys
import time

import github
import requests

import sorald._helpers


REPO_SLUG = "SpoonLabs/sorald"
REPO_SSH_URL = f"git@github.com:{REPO_SLUG}.git"
REPO_MAIN_BRANCH = "master"
RELEASE_BRANCH_TEMPLATE = "release/{}"

GITHUB_API_BASE_URL = "https://api.github.com"

GITHUB_TOKEN_ENV = "GITHUB_TOKEN"
REQUIRED_TOKEN_SCOPES = {"repo"}


def main() -> None:
    gh_token = os.getenv(GITHUB_TOKEN_ENV)
    if not gh_token:
        raise RuntimeError(f"Please set a GitHub token in {GITHUB_TOKEN_ENV}")
    _check_token_scopes(gh_token)

    _prepare_release()
    _push_release()

    # wait for deploy action to trigger for release before pushing next dev iteration
    time.sleep(30)

    _push_current_branch()
    _create_github_release(gh_token)


def _prepare_release() -> None:
    _run_cmd("mvn -B clean release:prepare -DpushChanges=false")
    _sign_last_n_commits(2)
    _sign_release_tag()


def _push_release() -> None:
    release_tag = _get_release_tag_name()
    _run_cmd(
        f"git push -u {REPO_SSH_URL} {release_tag}:{RELEASE_BRANCH_TEMPLATE.format(release_tag)} --tags"
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
            return tag_name
    raise RuntimeError("expected to find 'scm.tag' in release.properties")


def _check_token_scopes(token: str) -> None:
    gh = github.Github(login_or_token=token, base_url=GITHUB_API_BASE_URL)
    gh.get_rate_limit()  # make any get request to initialize the API wrapper
    if not REQUIRED_TOKEN_SCOPES.issubset(gh.oauth_scopes or {}):
        raise RuntimeError(f"access token must have scopes {REQUIRED_TOKEN_SCOPES}")


def _create_github_release(token: str):
    gh = github.Github(login_or_token=os.getenv(token), base_url=GITHUB_API_BASE_URL)
    repo = gh.get_repo("SpoonLabs/sorald")
    tag_name = _get_release_tag_name()
    release = repo.create_git_release(
        tag=tag_name, name=tag_name, message=f"Release {tag_name}"
    )
    release.upload_asset(
        sorald._helpers.DEFAULT_SORALD_JAR_PATH,
        content_type="application/zip",
        name=sorald._helpers.DEFAULT_SORALD_JAR_PATH.name,
    )


def _run_cmd(cmd) -> subprocess.CompletedProcess:
    proc = subprocess.run(shlex.split(cmd))
    proc.check_returncode()
    return proc


if __name__ == "__main__":
    main()
