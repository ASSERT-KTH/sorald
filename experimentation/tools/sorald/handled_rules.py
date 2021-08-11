import argparse
import dataclasses
import pathlib
import re
import sys

import jinja2

from typing import Dict, List, TypedDict, Union

from sorald._helpers import sonar_metadata, jsonkeys

ENCODING = "utf8"

TEMPLATE = r"""## Handled rules

Sorald can currently repair violations of the following rules:

* [Bug](#bug)
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
with open(PATH_TO_CLASSPATH, "r") as cp:
    CLASSPATH = cp.read()

PATH_TO_PROCESSOR_PACKAGE = pathlib.Path(__file__).absolute().parent.parent.parent.parent / "src/main/java/sorald/processor"


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
    extracted_output = extract_ouput_from_processor_package()
    structured_output = parse_raw_output(extracted_output)
    rendered_content = jinja2.Template(template).render(**structured_output)
    output_file.write_text(rendered_content, encoding=ENCODING)


class RawSoraldProcessorInformation(TypedDict):
    rule_key: int
    repair_description: str


def extract_ouput_from_processor_package() -> Dict[str, RawSoraldProcessorInformation]:
    raw_output = {}
    processors_and_descriptions =  pathlib.Path(PATH_TO_PROCESSOR_PACKAGE).glob("*")
    for proc_or_desc_file in processors_and_descriptions:
        if proc_or_desc_file.name == "SoraldAbstractProcessor.java":
            continue

        extension = proc_or_desc_file.suffix
        file_name = get_file_name_without_extension(proc_or_desc_file)

        info_from_sorald = raw_output.get(file_name)
        if info_from_sorald is None:
            raw_output[file_name] = { }
        
        if extension == ".md":
            raw_output[file_name]["repair_description"] = get_repair_description(proc_or_desc_file)

        elif extension == ".java":
            raw_output[file_name]["rule_key"] = get_rule_key_from_processor(proc_or_desc_file)

    return raw_output 

        

def get_file_name_without_extension(path: pathlib.Path) -> str:
    return path.name.split(".")[0]


def get_repair_description(path: pathlib.Path) -> str:
    return path.read_text(ENCODING)


def get_rule_key_from_processor(path: pathlib.Path) -> int:
    processor_code = path.read_text(ENCODING)
    regex = r"@ProcessorAnnotation\(\s*key\s*=\s*\"S(\d+)\""
    matches = re.search(regex, processor_code)
    return matches.group(1)


def parse_raw_output(raw_output: Dict[str, RawSoraldProcessorInformation]) \
    -> Dict[str, Union[List[ViolationList], List[ViolationDetail]]]:
    bugs = []
    bugs_detail = []
    code_smells = []
    code_smells_detail = []
    vulnerabilities = []
    vulnerabilities_detail = []

    for _, processor_information in raw_output.items():
        rule_key = processor_information["rule_key"]
        repair_description = processor_information["repair_description"]
        metadata = sonar_metadata.get_rule_metadata(rule_key)

        heading_text = f"{metadata[jsonkeys.SONAR_METADATA.TITLE]} (Sonar Rule {rule_key})"
        list_information = ViolationList(
            title=metadata[jsonkeys.SONAR_METADATA.TITLE],
            link_to_detail=get_link_to_detail(heading_text),
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
            bugs.append(list_information)
            bugs_detail.append(detail_information)

        elif violation_type == sonar_metadata.VIOLATION_TYPE.CODE_SMELL:
            code_smells.append(list_information)
            code_smells_detail.append(detail_information)

        elif violation_type == sonar_metadata.VIOLATION_TYPE.VULNERABILITY:
            vulnerabilities.append(list_information)
            vulnerabilities_detail.append(detail_information)

        else:
            raise Exception(f"New rule violation type, {violation_type}, encountered.")
    
    return {
        "bugs": bugs,
        "bugs_detail": bugs_detail,
        "code_smells": code_smells,
        "code_smells_detail": code_smells_detail,
        "vulnerabilities": vulnerabilities,
        "vulnerabilities_detail": vulnerabilities_detail,
    }


def get_link_to_detail(heading_text: str) -> str:
    sanitized_heading = re.sub(r"[^\sA-Za-z0-9_-]", "", heading_text)
    sanitized_heading_without_spaces = re.sub(r"\s", "-", sanitized_heading)
    return f"#{sanitized_heading_without_spaces.lower()}"


def get_sonar_link(rule_specification: str) -> str:
    return f"https://rules.sonarsource.com/java/{rule_specification}"


if __name__ == "__main__":
    main(sys.argv[1:])
