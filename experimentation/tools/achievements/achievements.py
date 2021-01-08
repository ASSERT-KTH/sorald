"""Script for generating the ACHIEVEMENTS.md file, which is a human-readable
version of the PRs json record.
"""
import dataclasses
import json
import pathlib
from typing import Sequence

import jinja2

CURRENT_DIR = pathlib.Path(__file__).parent
ENCODING = "utf8"


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
    repairs: Sequence[RepairStats]


def main():
    generate_achievements_file(
        CURRENT_DIR.parent / "pr_recorder" / "resources" / "prs_final.json"
    )


def generate_achievements_file(
    prs_json: pathlib.Path,
    output_file: pathlib.Path = CURRENT_DIR / "ACHIEVEMENTS.md",
    template_file: pathlib.Path = CURRENT_DIR / "ACHIEVEMENTS_TEMPLATE.jinja",
) -> None:
    pull_requests = parse_pull_requests(prs_json)
    rendered_content = get_template(template_file).render(pull_requests=pull_requests)
    output_file.write_text(rendered_content, encoding=ENCODING)


def parse_pull_requests(prs_json: pathlib.Path) -> Sequence[PullRequest]:
    prs_data = json.loads(prs_json.read_text(ENCODING))
    return [
        PullRequest(
            repo_slug=data["repo_slug"],
            number=pr_meta["number"],
            created_at=pr_meta["created_at"],
            closed_at=pr_meta["closed_at"] or pr_meta["merged_at"],
            status="merged" if pr_meta["is_merged"] else pr_meta["state"],
            contains_manual_edits=len(data["manual_edits"] or []) > 0,
            repairs=list(
                map(
                    parse_repair_stats,
                    sorted(
                        data["sorald_statistics"]["repairs"],
                        key=lambda rep: int(rep["ruleKey"]),
                    ),
                )
            ),
        )
        for _, data in prs_data.items()
        if (pr_meta := data["pr_metadata"])
    ]


def parse_repair_stats(repair_data: dict) -> RepairStats:
    return RepairStats(
        rule_key=int(repair_data["ruleKey"]),
        num_violations_found=repair_data["nbViolationsBefore"],
        num_violations_repaired=repair_data["nbViolationsBefore"]
        - repair_data["nbViolationsAfter"],
    )


def get_template(
    template_file: pathlib.Path,
) -> jinja2.Template:
    return jinja2.Template(template_file.read_text(ENCODING))


if __name__ == "__main__":
    main()
