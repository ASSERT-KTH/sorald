"""Script for searching the GitHub issues API."""
import pathlib
import argparse
import sys
import re
import logging

from typing import List

import github

GITHUB_API_URL = "https://api.github.com"


def main():
    parsed_args = create_parser().parse_args(sys.argv[1:])

    gh = github.Github(login_or_token=parsed_args.token, base_url=GITHUB_API_URL)

    search = gh.search_issues(
        parsed_args.query,
        sort=parsed_args.sort_by,
        order=parsed_args.order,
        language=parsed_args.language,
        state="open",
        type="issue",
    )

    for issue in search:
        print(issue.html_url)


def create_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="Tool to search GitHub issues for keywords",
        formatter_class=argparse.ArgumentDefaultsHelpFormatter,
    )

    parser.add_argument("-q", "--query", help="a free-form search query", required=True)
    parser.add_argument(
        "-t",
        "--token",
        help="a GitHub personal access token (no scopes required)",
        required=True,
    )
    parser.add_argument(
        "-l",
        "--language",
        help="the programming language to search for",
        default="java",
    )
    parser.add_argument(
        "-s",
        "--sort-by",
        help="what attribute to sort issues by",
        choices="comments created updated".split(),
        default="updated",
    )
    parser.add_argument(
        "-o",
        "--order",
        help="order of the results",
        choices="asc desc".split(),
        default="desc",
    )
    return parser


if __name__ == "__main__":
    main()
