"""Script for generating the ACHIEVEMENTS.md file, which is a human-readable
version of the PRs json record.
"""
import argparse
import dataclasses
import json
import pathlib
import sys
from typing import List

import jinja2

ENCODING = "utf8"

TEMPLATE = r"""# Achievements
This document presents an overview of the pull requests performed with Sorald.
{% for pr in pull_requests %}
## [{{ pr.repo_slug }}#{{ pr.number }}](https://github.com/{{ pr.repo_slug }}/pull/{{ pr.number }})
This PR was opened at {{ pr.created_at }}{% if pr.closed_at %} and {{ pr.status }} at {{ pr.closed_at }}{% endif %}.
{% if pr.contains_manual_edits %}Some manual edits were performed after applying Sorald{% else %}The patch was generated fully automatically with Sorald{% endif %}.
{% if pr.repairs|length > 0 %}
It provide{% if pr.closed_at %}d{% else %}s{% endif %} the following repairs:
{% for repair in pr.repairs %}
* [Rule {{ repair.rule_key }}](https://rules.sonarsource.com/java/RSPEC-{{ repair.rule_key }})
    - Number of violations found: {{ repair.num_violations_found }}
    - Number of violations repaired: {{ repair.num_violations_repaired }}{% endfor %}
{% else %}
Detailed repair information is missing for this PR.
{% endif %}{% endfor %}
"""

PRS_JSON_ARG = "--prs-json-file"
OUTPUT_ARG = "--output"


@dataclasses.dataclass
class RepairStats:
    rule_key: int
    num_violations_found: str
    num_violations_repaired: str


@dataclasses.dataclass
class PullRequest:
    repo_slug: str
    number: int
    created_at: str
    closed_at: str
    status: str
    contains_manual_edits: bool
    repairs: List[RepairStats]


def main(args: List[str]):
    parsed_args = parse_args(args)
    generate_achievements_file(
        prs_json=parsed_args.prs_json_file,
        output_file=parsed_args.output,
        template=TEMPLATE,
    )


def parse_args(args: List[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        prog="achievements",
        description="Script for generating the ACHIEVEMENTS.md file, "
        "detailing pull requests performed with Sorald.",
    )
    parser.add_argument(
        "-p",
        PRS_JSON_ARG,
        help="path to the prs.json file",
        type=pathlib.Path,
        required=True,
    )
    parser.add_argument(
        "-o",
        OUTPUT_ARG,
        help="path to the output Markdown file",
        type=pathlib.Path,
        required=True,
    )
    return parser.parse_args(args)


def generate_achievements_file(
    prs_json: pathlib.Path,
    output_file: pathlib.Path,
    template: str,
) -> None:
    pull_requests = parse_pull_requests(prs_json)
    rendered_content = jinja2.Template(template).render(pull_requests=pull_requests)
    output_file.write_text(rendered_content, encoding=ENCODING)


def parse_pull_requests(prs_json: pathlib.Path) -> List[PullRequest]:
    prs_data = json.loads(prs_json.read_text(ENCODING))
    return [
        PullRequest(
            repo_slug=data["repo_slug"],
            number=pr_meta["number"],
            created_at=pr_meta["created_at"],
            closed_at=pr_meta["closed_at"] or pr_meta["merged_at"],
            status="merged" if pr_meta["is_merged"] else pr_meta["state"],
            contains_manual_edits=len(data["manual_edits"] or []) > 0,
            repairs=get_all_repairs(data["sorald_statistics"]),
        )
        for _, data in prs_data.items()
        if (pr_meta := data["pr_metadata"])
    ]


def get_all_repairs(sorald_stats: dict) -> List[RepairStats]:
    lst = list(
        map(
            parse_repair_stats,
            sorted(
                sorald_stats.get("repairs") or [],
                key=lambda rep: int(rep["ruleKey"]),
            ),
        )
    )
    return lst


def parse_repair_stats(repair_data: dict) -> RepairStats:
    return RepairStats(
        rule_key=int(repair_data["ruleKey"]),
        num_violations_found=repair_data["nbViolationsBefore"],
        num_violations_repaired=repair_data["nbViolationsBefore"]
        - repair_data["nbViolationsAfter"],
    )


if __name__ == "__main__":
    main(sys.argv[1:])
