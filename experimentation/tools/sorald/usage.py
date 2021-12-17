import argparse
import pathlib
import sys
from typing import List

import jinja2

from sorald._helpers import soraldwrapper


ENCODING = "utf8"
TEMPLATE = r"""# `{{ subcommand }}`

> This file is generated using [usage.py](/experimentation/tools/sorald/usage.py).
> Please refrain from editing it manually.

```bash
{{ usage }}
```

"""

SUBCOMMAND_ARG = "--subcommand"
OUTPUT_ARG = "--output"


def main(args: List[str]):
    parsed_args = parse_args(args)
    generate_usage(
        subcommand=parsed_args.subcommand,
        output_file=parsed_args.output,
        template=TEMPLATE,
    )


def generate_usage(
    subcommand: soraldwrapper.SUBCOMMAND, output_file: pathlib.Path, template: str
):
    (exit_code, stdout, _) = soraldwrapper.sorald(subcommand, "--help")
    if exit_code == 0:
        rendered_content = jinja2.Template(template).render(
            subcommand=subcommand,
            usage=stdout.decode(ENCODING),
        )
        output_file.write_text(rendered_content, encoding=ENCODING)
    else:
        raise Exception("Process took too long to run.")


def parse_args(args: List[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        prog="usage",
        description="Script for generating the usage file for Sorald's " "subcommands",
    )
    parser.add_argument(
        "-s",
        SUBCOMMAND_ARG,
        help="subcommand whose usage needs to be generated",
        type=soraldwrapper.SUBCOMMAND,
        choices=list(soraldwrapper.SUBCOMMAND),
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


if __name__ == "__main__":
    main(sys.argv[1:])
