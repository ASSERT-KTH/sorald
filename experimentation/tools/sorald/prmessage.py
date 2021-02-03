"""Script for generating a PR message for sorald."""
import argparse
import requests
import sys
import re

from typing import List

from bs4 import BeautifulSoup

import jinja2

TEMPLATE = r"""PULL REQUEST TITLE: Fix violations of Sonar rule {{ rule_key }}
Hi,

This PR fixes {{ num_repairs }} violations of [Sonar Rule {{ rule_key }}: '{{ rule_description }}'](https://rules.sonarsource.com/java/RSPEC-{{ rule_key }}).

The patch was generated automatically with the tool [Sorald](https://github.com/SpoonLabs/sorald). For details on the fix applied here, please see [Sorald's documentation on rule {{ rule_key }}]({{ rule_doc_url }}).
"""

SONAR_VERSION = "6.9.0.23563"
SONAR_RULE_METADATA_URL_TEMPLATE = (
    "https://raw.githubusercontent.com/SonarSource/sonar-java/"
    f"/{SONAR_VERSION}/java-checks/src/main/resources"
    "/org/sonar/l10n/java/rules/java/S{rule_key}_java.json"
)

HANDLED_RULES_URL = (
    "https://github.com/SpoonLabs/sorald/blob/master/docs/HANDLED_RULES.md"
)


def main(args: List[str]):
    parser = argparse.ArgumentParser(
        prog="sorald.prmessage",
        description="Script that generates a pull request message for a Sorald "
        "repair of a single rule.",
    )
    parser.add_argument(
        "--rule-key",
        help="numerical rule key for the Sonar rule (don't include 'S' prefix!)",
        type=int,
        required=True,
    )
    parser.add_argument(
        "--num-repairs", help="amount of repairs performed", type=int, required=True
    )

    parsed_args = parser.parse_args(args)

    print(generate_pr_message(parsed_args.rule_key, parsed_args.num_repairs))


def generate_pr_message(rule_key: int, num_repairs: int) -> str:
    rule_description = get_rule_description(rule_key)
    rule_doc_url = get_rule_doc_url(rule_key)

    return jinja2.Template(TEMPLATE).render(
        rule_key=rule_key,
        rule_description=rule_description,
        rule_doc_url=rule_doc_url,
        num_repairs=num_repairs,
    )


def get_rule_description(rule_key: int) -> str:
    return requests.get(
        SONAR_RULE_METADATA_URL_TEMPLATE.format(rule_key=rule_key)
    ).json()["title"]


def get_rule_doc_url(rule_key: int, handled_rules_url: str = HANDLED_RULES_URL) -> str:
    handled_rules = requests.get(handled_rules_url).content.decode()
    markup = BeautifulSoup(handled_rules, features="html.parser")

    for a_tag in markup.find_all("a", class_="anchor"):
        if a_tag.attrs["id"].endswith(f"sonar-rule-{rule_key}"):
            return f"{handled_rules_url}{a_tag.attrs['href']}"

    raise ValueError(f"No handled rule with key {rule_key}")


if __name__ == "__main__":
    main(sys.argv[1:])
