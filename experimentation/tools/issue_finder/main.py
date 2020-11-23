"""Script for finding issues by keyword (searching title and body)."""
import pathlib
import argparse
import sys
import re

from typing import List

import github

# parts to search in
TITLE = "title"
BODY = "body"
ALL = "all"

GITHUB_API_URL = "https://api.github.com"


def main():
    parsed_args = create_parser().parse_args(sys.argv[1:])

    repo_fullnames = [
        stripped_line
        for line in parsed_args.repo_list.read_text(
            encoding=sys.getdefaultencoding()
        ).split("\n")
        if (stripped_line := line.strip())
    ]

    gh = github.Github(login_or_token=parsed_args.token, base_url=GITHUB_API_URL)

    search_in_title = parsed_args.search_in in (TITLE, ALL)
    search_in_body = parsed_args.search_in in (BODY, ALL)

    for repo_fullname in repo_fullnames:
        repo = gh.get_repo(repo_fullname)
        matching_issues = (
            issue
            for issue in repo.get_issues(state="open")
            if issue_is_match(
                issue, ["eventually realize"], search_in_title, search_in_body
            )
        )

        for issue in matching_issues:
            print(f"Match: {repo_fullname}#{issue.number}")


def issue_is_match(
    issue: github.Issue.Issue,
    keywords: List[str],
    search_in_title: bool,
    search_in_body: bool,
):
    assert search_in_title or search_in_body

    text = f'{(issue.title if search_in_title else "")}\n{(issue.body if search_in_body else "")}'
    regex = "|".join(keywords)

    return re.search(regex, text, re.IGNORECASE)


def create_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="Tool to search GitHub issues for keywords.",
        formatter_class=argparse.ArgumentDefaultsHelpFormatter,
    )

    parser.add_argument(
        "-t", "--token", help="a GitHub personal access token", required=True
    )
    parser.add_argument(
        "-r",
        "--repo-list",
        help="a plaintext file with GitHub repositories, one on each line on "
        "the form <OWNER>/<REPO> (e.g. spoonlabs/sorald)",
        required=True,
        type=pathlib.Path,
    )
    parser.add_argument(
        "--search-in",
        help="which part of the issues to search for the keyword(s) in",
        choices=[TITLE, BODY, ALL],
        default=TITLE,
    )
    return parser


if __name__ == "__main__":
    main()
