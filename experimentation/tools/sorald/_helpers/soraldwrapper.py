"""Python wrapper around the Sorald CLI."""
import subprocess
import itertools
import pathlib
import re
import sys

from typing import List


DEFAULT_SORALD_JAR_PATH = (
    pathlib.Path(__file__).absolute().parent.parent.parent.parent.parent
    / "target"
    / "sorald-1.1-SNAPSHOT-jar-with-dependencies.jar"
).resolve(strict=False)


def sorald(
    subcommand: str,
    *args,
    sorald_jar: pathlib.Path = DEFAULT_SORALD_JAR_PATH,
    **kwargs,
) -> subprocess.CompletedProcess:
    """Wrapper around the Sorald cli.

    Note that iterables (not including strings) passed as values are converted into
    comma-separated strings, as that's what Sorald currently uses.

    Args:
        subcommand: The subcommand to execute.
        args: Positional arguments. Each positional argument is converted into a string.
        sorald_jar: Path to the sorald jarfile.
        kwargs: Keyword arguments. Each keyword argument ``some_key=value`` is converted
            into ``"--some-key value``, where ``value`` is first evaluated as a string.
    Returns:
        The completed subprocess, with captured output.
    """
    cmd = [
        "java",
        "-jar",
        str(sorald_jar),
        subcommand,
        *list(map(_to_cli_arg, args)),
        *list(
            itertools.chain.from_iterable(
                [
                    (f"--{key.replace('_', '-')}", _to_cli_arg(value))
                    for key, value in kwargs.items()
                ]
            )
        ),
    ]
    return subprocess.run(cmd, capture_output=True)


def available_rule_keys(
    sorald_jar: pathlib.Path = DEFAULT_SORALD_JAR_PATH,
) -> List[str]:
    """
    Args:
        sorald_jar: Path to the Sorald jarfile.
    Returns:
        The available rule keys in the given jarfile.
    """
    output = sorald("repair", "--help")
    output_lines = iter(output.stdout.decode(sys.getdefaultencoding()).split("\n"))

    dropped_initial_lines = itertools.dropwhile(
        lambda line: not line.strip().startswith("--rule-keys"), output_lines
    )
    next(dropped_initial_lines)  # drop --rule-keys line
    return [
        match.group()
        for line in itertools.takewhile(
            lambda l: not l.strip().startswith("--"), dropped_initial_lines
        )
        if (match := re.search(r"^\d+(?=:)", line.strip()))
    ]


def _to_cli_arg(v) -> str:
    if hasattr(v, "__iter__") and not isinstance(v, str):
        return ",".join(map(str, v))
    else:
        return str(v)
