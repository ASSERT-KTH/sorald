import argparse
import dataclasses
import json
import pathlib
import re
import subprocess
import sys

import jinja2

from os.path import dirname, abspath, join
from typing import Dict, List, Union

from sorald._helpers import sonar_metadata, jsonkeys

ENCODING = "utf8"

BUG = []
BUG_DETAIL = []

CODE_SMELL = []
CODE_SMELL_DETAIL = []

VULNERABILITY = []
VULNERABILITY_DETAIL = []

TEMPLATE = r"""## Handled rules

Sorald can currently repair violations of the following rules:

* [Bug][#bug]
{% for bug in bugs %}
    * [{{ bug.title }}]({{ bug.link_to_detail }}) ([{{ bug.sonar_url_text }}]({{ bug.sonar_url }})) {% endfor %}

* [Code Smell](#code-smell)
{% for cs in code_smells %}
    * [{{ cs.title }}]({{ cs.link_to_detail }}) ([{{ cs.sonar_url_text }}]({{ cs.sonar_url }})) {% endfor %}

* [Vulnerability](#vulnerability)
{% for vulnerability in vulnerabilities %}
    * [{{ vulnerability.title }}]({{ vulnerability.link_to_detail }}) ([{{ vulnerability.sonar_url_text }}]({{ vulnerability.sonar_url }})) {% endfor %}

### *Bug*
{% for bug_content in bugs_detail %}
#### {{ bug_content.title }} ([{{ bug_content.sonar_url_text }}]({{ bug_content.sonar_url }}))

{{ bug_content.repair_description }}

-----
{% endfor %}
### *Code Smell*
{% for cs_content in code_smells_detail %}
#### {{ cs_content.title }} ([{{ cs_content.sonar_url_text }}]({{ cs_content.sonar_url }}))

{{ cs_content.repair_description }}

-----
{% endfor %}
### *Vulnerability*
{% for vulnerability_content in vulnerabilities_detail %}
#### {{ vulnerability_content.title }} ([{{ vulnerability_content.sonar_url_text }}]({{ vulnerability_content.sonar_url }}))

{{ vulnerability_content.repair_description }}

-----
{% endfor %}
"""

OUTPUT_ARG = "--output"

PATH_TO_CLASSPATH = pathlib.Path(__file__).absolute().parent.parent.parent.parent / "cp.txt"
with open(PATH_TO_CLASSPATH, 'r') as cp:
    CLASSPATH = cp.read()

PATH_TO_JAVA_SCRIPT = join(dirname(dirname(abspath(__file__))), "scripts", "GetKeyAndDescription.java")
PATH_TO_PROCESSOR_PACKAGE = pathlib.Path(__file__).absolute().parent.parent.parent.parent / "src/main/java/sorald/processor"


@dataclasses.dataclass
class SoraldProcessorInformation:
    rule_key: int
    repair_description: str


@dataclasses.dataclass
class ViolationList:
    title: str
    link_to_detail: str
    sonar_url: str
    sonar_url_text: str


@dataclasses.dataclass
class ViolationDetail:
    title: str
    sonar_url: str
    sonar_url_text: str
    repair_description: str


def main(args: List[str]):
    parsed_args = parse_args(args)
    generate_handled_rules(
        output_file=parsed_args.output,
        template=TEMPLATE,
    )


def parse_args(args: List[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        prog="handled_rules",
        description="Script for generating the HANDLED_RULES.md file, "
        "detailing violations which are currently repaired by Sorald.",
    )
    parser.add_argument(
        "-o",
        OUTPUT_ARG,
        help="path to the output Markdown file",
        type=pathlib.Path,
        required=True,
    )
    return parser.parse_args(args)


def generate_handled_rules(
    output_file: pathlib.Path,
    template: str,
) -> None:
    spoon_output = get_output_from_spoon()
    parse_spoon_output(spoon_output)
    rendered_content = jinja2.Template(template).render(
        bugs=BUG,
        bugs_detail=BUG_DETAIL,
        code_smells=CODE_SMELL,
        code_smells_detail=CODE_SMELL_DETAIL,
        vulnerabilities=VULNERABILITY,
        vulnerabilities_detail=VULNERABILITY_DETAIL,
    )
    output_file.write_text(rendered_content, encoding=ENCODING)


def get_output_from_spoon() -> List[SoraldProcessorInformation]:
    command = f"java -cp {CLASSPATH} {PATH_TO_JAVA_SCRIPT} {PATH_TO_PROCESSOR_PACKAGE}"
    pipe = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    output, error = pipe.communicate()
    if error:
        raise Exception(error.decode(ENCODING))
    return [SoraldProcessorInformation(**processor) for processor in json.loads(output.decode(ENCODING))]


def parse_spoon_output(spoon_output: List[SoraldProcessorInformation]) -> None:
    for processor in spoon_output:
        classify(
            sonar_metadata.get_rule_metadata(processor.rule_key),
            processor.rule_key,
            processor.repair_description,
        )


def classify(metadata: Dict[str, Union[str, List[str]]], rule_key: int, repair_description: str) -> None:
    list_information = ViolationList(
        title=metadata[jsonkeys.SONAR_METADATA.TITLE],
        link_to_detail=get_link_to_detail(metadata[jsonkeys.SONAR_METADATA.TITLE], rule_key),
        sonar_url=get_sonar_link(metadata[jsonkeys.SONAR_METADATA.RULE_SPECIFICATION]),
        sonar_url_text=f"Sonar Rule {rule_key}",
    )
    detail_information = ViolationDetail(
        title=metadata[jsonkeys.SONAR_METADATA.TITLE],
        sonar_url=get_sonar_link(metadata[jsonkeys.SONAR_METADATA.RULE_SPECIFICATION]),
        sonar_url_text=f"Sonar Rule {rule_key}",
        repair_description=repair_description,
    )

    violation_type = metadata[jsonkeys.SONAR_METADATA.TYPE]
    if violation_type == sonar_metadata.VIOLATION_TYPE.BUG:
        BUG.append(list_information)
        BUG_DETAIL.append(detail_information)

    elif violation_type == sonar_metadata.VIOLATION_TYPE.CODE_SMELL:
        CODE_SMELL.append(list_information)
        CODE_SMELL_DETAIL.append(detail_information)

    elif violation_type == sonar_metadata.VIOLATION_TYPE.VULNERABILITY:
        VULNERABILITY.append(list_information)
        VULNERABILITY_DETAIL.append(detail_information)

    else:
        raise Exception(f"New rule violation type, {violation_type}, encountered.")


def get_link_to_detail(title: str, rule_key: int) -> str:
    link = title.lower()
    link = re.sub(r"\s", "-", link)
    link = re.sub(r"[^A-Za-z0-9_-]", "", link)
    return f"#{link}-sonar-rule-{rule_key}"


def get_sonar_link(rule_specification: str) -> str:
    return f"https://rules.sonarsource.com/java/{rule_specification}"


if __name__ == "__main__":
    main(sys.argv[1:])
