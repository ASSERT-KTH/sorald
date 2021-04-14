"""Script for breaking CI builds if Sorald finds a repairable violation."""
import argparse
import json
import pathlib
import shutil
import tempfile
import sys

from typing import List

from sorald._helpers import soraldwrapper, jsonkeys


def run(args: List[str]) -> int:
    """Executes the buildbreaker script.

    Args:
        args: Command line arguments, excluding the name of the script.
    Returns:
        The exit code, which is non-zero if there are any repairable
        violations.
    """
    parser = argparse.ArgumentParser(
        prog="sorald.buildbreaker",
        description="Script for breaking CI builds if Sorald finds a repairable violation",
    )
    parser.add_argument(
        "-s",
        "--source",
        help="path to the source root of the project to analyze (must be a directory)",
        type=pathlib.Path,
        default=pathlib.Path("."),
    )
    parsed_args = parser.parse_args(args)

    if not parsed_args.source.is_dir():
        print("--source must point to a directory", file=sys.stderr)
        return -1

    with tempfile.TemporaryDirectory() as tmpdir:
        workdir = pathlib.Path(tmpdir)
        mining_file = workdir / "mine.json"

        # mine handled rules
        original_source = parsed_args.source.resolve(strict=True)
        source = workdir / "project"
        shutil.copytree(original_source, source)
        rc, _, stderr = soraldwrapper.sorald(
            "mine",
            "--handled-rules",
            source=source,
            stats_output_file=mining_file,
        )
        assert rc == 0, stderr

        # for each rule, extract all violations
        mined_data = json.loads(mining_file.read_text())
        repaired_violation_specs = []
        for mined_rule_data in mined_data["minedRules"]:
            rule_key = mined_rule_data[jsonkeys.SORALD_STATS.RULE_KEY]
            stats_output_file = workdir / f"{rule_key}.json"
            specifiers = soraldwrapper.OPTION_LIST_SEP.join(
                [
                    location["violationSpecifier"]
                    for location in mined_rule_data["warningLocations"]
                ]
            )

            # repair all violations of a given rule
            rc, _, stderr = soraldwrapper.sorald(
                "repair",
                source=source,
                violation_specs=specifiers,
                stats_output_file=stats_output_file,
            )

            if rc != 0:
                raise RuntimeError(f"Sorald encountered an error: {stderr}")
            else:
                repair_data = json.loads(stats_output_file.read_text())
                for rule_repair_data in repair_data[jsonkeys.SORALD_STATS.REPAIRS]:
                    num_succesful_repairs = (
                        rule_repair_data[jsonkeys.SORALD_STATS.VIOLATIONS_BEFORE]
                        - rule_repair_data[jsonkeys.SORALD_STATS.VIOLATIONS_AFTER]
                    )

                    if num_succesful_repairs > 0:
                        performed_repairs = rule_repair_data[
                            jsonkeys.SORALD_STATS.PERFORMED_REPAIRS_LOCATIONS
                        ]
                        performed_repair_specs = [
                            repair["violationSpecifier"] for repair in performed_repairs
                        ]
                        repaired_violation_specs.extend(performed_repair_specs)

        if repaired_violation_specs:
            print(
                f"There were succesful repairs: {' '.join(performed_repair_specs)}",
                file=sys.stderr,
            )
            return -1
        else:
            print("No repairable violations found")
            return 0


if __name__ == "__main__":
    run(sys.argv[1:])
