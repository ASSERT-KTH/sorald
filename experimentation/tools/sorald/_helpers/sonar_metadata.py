"""Utility for fetching SonarSource metadata"""
import requests

from typing import Dict, List, Union

SONAR_VERSION = "6.9.0.23563"
SONAR_RULE_METADATA_URL_TEMPLATE = (
    "https://raw.githubusercontent.com/SonarSource/sonar-java/"
    f"/{SONAR_VERSION}/java-checks/src/main/resources"
    "/org/sonar/l10n/java/rules/java/S{rule_key}_java.json"
)


class VIOLATION_TYPE:
    BUG = "BUG"
    CODE_SMELL = "CODE_SMELL"
    VULNERABILITY = "VULNERABILITY"


def get_rule_metadata(rule_key: int) -> Dict[str, Union[str, List[str]]]:
    return requests.get(
        SONAR_RULE_METADATA_URL_TEMPLATE.format(rule_key=rule_key)
    ).json()
