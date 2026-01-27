"""Script for generating a PR message for sorald."""
import argparse
import requests
import sys
import re

from typing import List
import json

from bs4 import BeautifulSoup

import jinja2

from sorald._helpers import jsonkeys
from sorald._helpers.sonar_metadata import get_rule_metadata

TEMPLATE = r"""PULL REQUEST TITLE: Fix violations of Sonar rule {{ rule_key }}
Hi,

This PR fixes {{ num_repairs }} violations of [Sonar Rule {{ rule_key }}: '{{ rule_description }}'](https://rules.sonarsource.com/java/RSPEC-{{ rule_key }}).

The patch was generated automatically with the tool [Sorald](https://github.com/SpoonLabs/sorald). For details on the fix applied here, please see [Sorald's documentation on rule {{ rule_key }}]({{ rule_doc_url }}).
"""

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
    rule_description = get_rule_metadata(rule_key)[jsonkeys.SONAR_METADATA.TITLE]
    rule_doc_url = get_rule_doc_url(rule_key)

    return jinja2.Template(TEMPLATE).render(
        rule_key=rule_key,
        rule_description=rule_description,
        rule_doc_url=rule_doc_url,
        num_repairs=num_repairs,
    )


def get_rule_doc_url(rule_key: int, handled_rules_url: str = HANDLED_RULES_URL) -> str:
    handled_rules = requests.get(handled_rules_url, headers={"Content-Type": "text/html"}).content.decode()
    
    # Try to extract anchor from GitHub's React embedded JSON
    match = re.search(r'<script type="application/json" data-target="react-app.embeddedData">(.+?)</script>', handled_rules, re.DOTALL)
    if match:
        try:
            json_str = match.group(1)
            data = json.loads(json_str)
            
            # Search for the anchor in the JSON structure recursively
            def find_anchor(obj, target_key):
                """Recursively search for anchor matching the rule key."""
                if isinstance(obj, dict):
                    # Check if this dict has an 'anchor' key with our rule
                    if 'anchor' in obj and f'sonar-rule-{target_key}' in obj['anchor']:
                        return obj['anchor']
                    # Recursively search all values
                    for value in obj.values():
                        result = find_anchor(value, target_key)
                        if result:
                            return result
                elif isinstance(obj, list):
                    # Recursively search all items
                    for item in obj:
                        result = find_anchor(item, target_key)
                        if result:
                            return result
                return None
            
            anchor = find_anchor(data, rule_key)
            if anchor:
                return f"{handled_rules_url}#{anchor}"
        except (json.JSONDecodeError, KeyError):
            pass  # Fall through to legacy parsing
    
    # Legacy parsing (for non-GitHub URLs or if JSON parsing fails)
    markup = BeautifulSoup(handled_rules, features="html.parser")
    for a_tag in markup.find_all("a"):
        href = a_tag.get("href")
        if href and f"sonar-rule-{rule_key}" in href:
            unescaped_attr = a_tag.attrs["href"].replace("\\\"", "")
            return f"{handled_rules_url}{unescaped_attr}"

    raise ValueError(f"No handled rule with key {rule_key}")


if __name__ == "__main__":
    main(sys.argv[1:])
