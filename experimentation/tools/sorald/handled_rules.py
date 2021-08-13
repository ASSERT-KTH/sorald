import argparse
import dataclasses
import pathlib
import re
import sys

import jinja2

from typing import Dict, List, Union

from sorald._helpers import sonar_metadata, jsonkeys

ENCODING = "utf8"

TEMPLATE = r"""## Handled rules

> This file is generated using [handled_rules.py](experimentation/tools/sorald/handled_rules.py).
> Please refrain from editing it manually.

Sorald can currently repair violations of the following rules:

* [Bug](#bug)
{% for bug in bugs %}
    * [{{ bug.title }}]({{ bug.link_to_repair_description }}) ([{{ bug.sonar_url_text }}]({{ bug.sonar_url }})) {% endfor %}

* [Code Smell](#code-smell)
{% for cs in code_smells %}
    * [{{ cs.title }}]({{ cs.link_to_repair_description }}) ([{{ cs.sonar_url_text }}]({{ cs.sonar_url }})) {% endfor %}

* [Vulnerability](#vulnerability)
{% for vulnerability in vulnerabilities %}
    * [{{ vulnerability.title }}]({{ vulnerability.link_to_repair_description }}) ([{{ vulnerability.sonar_url_text }}]({{ vulnerability.sonar_url }})) {% endfor %}

### *Bug*
{% for bug_content in bugs %}
#### {{ bug_content.title }} ([{{ bug_content.sonar_url_text }}]({{ bug_content.sonar_url }}))

{{ bug_content.repair_description }}

-----
{% endfor %}
### *Code Smell*
{% for cs_content in code_smells %}
#### {{ cs_content.title }} ([{{ cs_content.sonar_url_text }}]({{ cs_content.sonar_url }}))

{{ cs_content.repair_description }}

-----
{% endfor %}
### *Vulnerability*
{% for vulnerability_content in vulnerabilities %}
#### {{ vulnerability_content.title }} ([{{ vulnerability_content.sonar_url_text }}]({{ vulnerability_content.sonar_url }}))

{{ vulnerability_content.repair_description }}

-----
{% endfor %}
"""

OUTPUT_ARG = "--output"

PATH_TO_PROCESSOR_PACKAGE = (
    pathlib.Path(__file__).absolute().parent.parent.parent.parent
    / "src/main/java/sorald/processor"
)


@dataclasses.dataclass
class RawSoraldProcessorInformation:
    repair_description: str
    rule_key: int


@dataclasses.dataclass
class ViolationInformation:
    title: str
    sonar_url: str
    sonar_url_text: str
    repair_description: str
    link_to_repair_description: str

    def __lt__(self, other):
        return self.title.lower() < other.title.lower()


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
    extracted_output = extract_ouput_from_processor_package()
    structured_output = parse_raw_output(extracted_output)
    rendered_content = jinja2.Template(template).render(**structured_output)
    output_file.write_text(rendered_content, encoding=ENCODING)


def extract_ouput_from_processor_package() -> List[RawSoraldProcessorInformation]:
    raw_output: List[RawSoraldProcessorInformation] = []
    processors = pathlib.Path(PATH_TO_PROCESSOR_PACKAGE).glob("*.java")
    for processor in processors:
        if processor.name == "SoraldAbstractProcessor.java":
            continue

        raw_output.append(
            RawSoraldProcessorInformation(
                repair_description=get_repair_description(processor),
                rule_key=get_rule_key(processor),
            )
        )

    return raw_output


def get_repair_description(path: pathlib.Path) -> str:
    parent_directory = path.parent
    processor_name = path.name.split(".")[0]
    description_file = parent_directory / f"{processor_name}.md"
    return description_file.read_text()


def get_rule_key(path: pathlib.Path) -> int:
    processor_code = path.read_text(ENCODING)
    regex = r"@ProcessorAnnotation\((.|\r|\n)*key\s*=\s*\"S(\d+)\""
    matches = re.search(regex, processor_code)
    return int(matches.group(2))


def parse_raw_output(
    raw_output: List[RawSoraldProcessorInformation],
) -> Dict[str, Union[List[ViolationInformation]]]:
    bugs = []
    code_smells = []
    vulnerabilities = []

    for processor_information in raw_output:
        repair_description = processor_information.repair_description
        rule_key = processor_information.rule_key
        metadata = sonar_metadata.get_rule_metadata(rule_key)

        heading_text = f"{metadata[jsonkeys.SONAR_METADATA.TITLE]} ({get_sonar_link_text(rule_key)})"
        violation_information = ViolationInformation(
            title=metadata[jsonkeys.SONAR_METADATA.TITLE],
            sonar_url=get_sonar_link(
                metadata[jsonkeys.SONAR_METADATA.RULE_SPECIFICATION]
            ),
            sonar_url_text=get_sonar_link_text(rule_key),
            repair_description=repair_description,
            link_to_repair_description=get_link_to_repair_description(heading_text),
        )

        violation_type = metadata[jsonkeys.SONAR_METADATA.TYPE]
        if violation_type == sonar_metadata.VIOLATION_TYPE.BUG:
            bugs.append(violation_information)

        elif violation_type == sonar_metadata.VIOLATION_TYPE.CODE_SMELL:
            code_smells.append(violation_information)

        elif violation_type == sonar_metadata.VIOLATION_TYPE.VULNERABILITY:
            vulnerabilities.append(violation_information)

        else:
            raise Exception(f"New rule violation type, {violation_type}, encountered.")

    return {
        "bugs": sorted(bugs),
        "code_smells": sorted(code_smells),
        "vulnerabilities": sorted(vulnerabilities),
    }


def get_link_to_repair_description(heading_text: str) -> str:
    sanitized_heading = re.sub(r"[^\sA-Za-z0-9_-]", "", heading_text)
    sanitized_heading_without_spaces = re.sub(r"\s", "-", sanitized_heading)
    return f"#{sanitized_heading_without_spaces.lower()}"


def get_sonar_link_text(rule_key: int) -> str:
    return f"Sonar Rule {rule_key}"


def get_sonar_link(rule_specification: str) -> str:
    return f"https://rules.sonarsource.com/java/{rule_specification}"


if __name__ == "__main__":
    main(sys.argv[1:])
