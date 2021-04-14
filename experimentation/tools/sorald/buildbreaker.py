"""Script for breaking CI builds if Sorald finds a repairable violation."""
import argparse
import json
import pathlib
import shutil
import tempfile
import sys
import secrets

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
    parser.add_argument(
        "-j",
        "--sorald-jar",
        help="path to the Sorald JAR",
        type=pathlib.Path,
        default=soraldwrapper.DEFAULT_SORALD_JAR_PATH,
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
            sorald_jar=parsed_args.sorald_jar,
        )
        assert rc == 0, stderr

        # for each rule, extract all violations
        mined_data = json.loads(mining_file.read_text())
        repaired_violation_specs = _attempt_repairs(
            mined_data, source, parsed_args.sorald_jar
        )

        if repaired_violation_specs:
            print(
                f"There were succesful repairs: {' '.join(repaired_violation_specs)}",
                file=sys.stderr,
            )
            return -1
        else:
            print("No repairable violations found")
            return 0


def _attempt_repairs(
    mined_data: dict, source: pathlib.Path, sorald_jar: pathlib.Path
) -> List[str]:
    """Attempt to perform targeted repairs of all mined violations at the given
    source site.
    """
    repaired_violation_specs = []
    for mined_rule_data in mined_data["minedRules"]:
        specifiers = soraldwrapper.OPTION_LIST_SEP.join(
            [
                location["violationSpecifier"]
                for location in mined_rule_data["warningLocations"]
            ]
        )

        repaired_violation_specs.extend(
            _targeted_repair(source, specifiers, sorald_jar)
        )

    return repaired_violation_specs


def _targeted_repair(
    source: str, violation_specs: List[str], sorald_jar: pathlib.Path
) -> List[str]:
    """Perform targeted repair of the violation specs and return the violation
    specs of repaired violations.
    """
    stats_output_file = source / f"{secrets.token_hex()}.json"
    rc, _, stderr = soraldwrapper.sorald(
        "repair",
        source=source,
        violation_specs=violation_specs,
        stats_output_file=stats_output_file,
        sorald_jar=sorald_jar,
    )

    if rc != 0:
        raise RuntimeError(f"Sorald encountered an error: {stderr}")
    else:
        sorald_repair_stats = json.loads(stats_output_file.read_text())
        return _parse_repaired_violation_specs(sorald_repair_stats)


def _parse_repaired_violation_specs(sorald_repair_stats: dict) -> List[str]:
    rule_repairs = sorald_repair_stats[jsonkeys.SORALD_STATS.REPAIRS]

    if not rule_repairs:
        return []
    elif len(rule_repairs) > 1:
        raise RuntimeError(f"Expected repairs for a single rule, found: {rule_repairs}")

    rule_repair_data = rule_repairs[0]

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

        # NOTE: This is an approximation, it's not certain that all performed
        # repairs were succesful, but we know that at least one was.
        return performed_repair_specs
    else:
        return []


if __name__ == "__main__":
    sys.exit(run(sys.argv[1:]))
