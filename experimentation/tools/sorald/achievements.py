"""Script for generating the ACHIEVEMENTS.md file, which is a human-readable
version of the PRs json record.
"""
import argparse
import dataclasses
import json
import pathlib
import sys
import datetime
from typing import List

import jinja2

from sorald._helpers import jsonkeys

ENCODING = "utf8"

TEMPLATE = r"""# Achievements
This document presents an overview of the pull requests performed with Sorald.
{% for pr in pull_requests %}
## [{{ pr.repo_slug }}#{{ pr.number }}](https://github.com/{{ pr.repo_slug }}/pull/{{ pr.number }})
This PR was opened at {{ pr.created_at }}{% if pr.closed_at %} and {{ pr.status }} at {{ pr.closed_at }}{% endif %}.{% if pr.contains_manual_edits %}
Some manual edits were performed after applying Sorald.{% elif not pr.is_legacy %}
The patch was generated fully automatically with Sorald.{% else %}
This is a legacy PR made before detailed record-keeping, and so we cannot say if any manual edits have been applied.{% endif %}
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
    is_legacy: bool


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
    pull_requests = sorted(
        parse_pull_requests(prs_json),
        key=lambda pr: datetime.datetime.fromisoformat(pr.created_at),
        reverse=True,
    )
    rendered_content = jinja2.Template(template).render(pull_requests=pull_requests)
    output_file.write_text(rendered_content, encoding=ENCODING)


def parse_pull_requests(prs_json: pathlib.Path) -> List[PullRequest]:
    prs_data = json.loads(prs_json.read_text(ENCODING))

    def _is_legacy(data: dict) -> bool:
        return data[jsonkeys.RECORD.SECTION_KEY][jsonkeys.RECORD.IS_LEGACY]

    return [
        PullRequest(
            repo_slug=data[jsonkeys.TOP_LEVEL.REPO_SLUG],
            number=pr_meta[jsonkeys.PR.NUMBER],
            created_at=pr_meta[jsonkeys.PR.CREATED_AT],
            closed_at=pr_meta[jsonkeys.PR.CLOSED_AT] or pr_meta[jsonkeys.PR.MERGED_AT],
            status="merged"
            if pr_meta[jsonkeys.PR.IS_MERGED]
            else pr_meta[jsonkeys.PR.STATE],
            contains_manual_edits=len(data[jsonkeys.MANUAL_EDITS.SECTION_KEY] or [])
            > 0,
            repairs=get_all_repairs(
                data[jsonkeys.SORALD_STATS.SECTION_KEY], _is_legacy(data)
            ),
            is_legacy=_is_legacy(data),
        )
        for _, data in prs_data.items()
        if (pr_meta := data[jsonkeys.PR.SECTION_KEY])
    ]


def get_all_repairs(sorald_stats: dict, is_legacy: bool) -> List[RepairStats]:
    return (
        [parse_legacy_repair_stats(sorald_stats)]
        if is_legacy
        else list(
            map(
                parse_repair_stats,
                sorted(
                    sorald_stats.get(jsonkeys.SORALD_STATS.REPAIRS) or [],
                    key=lambda rep: int(rep[jsonkeys.SORALD_STATS.RULE_KEY]),
                ),
            )
        )
    )


def parse_repair_stats(repair_data: dict) -> RepairStats:
    return RepairStats(
        rule_key=int(repair_data[jsonkeys.SORALD_STATS.RULE_KEY]),
        num_violations_found=repair_data[jsonkeys.SORALD_STATS.VIOLATIONS_BEFORE],
        num_violations_repaired=repair_data[jsonkeys.SORALD_STATS.VIOLATIONS_BEFORE]
        - repair_data[jsonkeys.SORALD_STATS.VIOLATIONS_AFTER],
    )


def parse_legacy_repair_stats(repair_data: dict) -> RepairStats:
    num_violations = repair_data[jsonkeys.SORALD_STATS.LEGACY.NUM_VIOLATIONS]
    return RepairStats(
        rule_key=int(repair_data[jsonkeys.SORALD_STATS.LEGACY.RULE_KEY]),
        num_violations_found=num_violations,
        num_violations_repaired=num_violations,
    )


if __name__ == "__main__":
    main(sys.argv[1:])
