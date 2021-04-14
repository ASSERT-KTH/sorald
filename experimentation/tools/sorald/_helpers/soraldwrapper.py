"""Python wrapper around the Sorald CLI."""
import subprocess
import itertools
import pathlib
import re
import sys

from typing import List, Optional, Tuple


def _find_default_sorald_jar() -> pathlib.Path:
    target_dir = (
        pathlib.Path(__file__).absolute().parent.parent.parent.parent.parent / "target"
    )
    sorald_jar_matches = list(
        target_dir.glob("sorald-*-SNAPSHOT-jar-with-dependencies.jar")
    )

    if not sorald_jar_matches:
        return pathlib.Path("DEFAULT_JAR_MISSING")

    return sorald_jar_matches[0].resolve(strict=True)


DEFAULT_SORALD_JAR_PATH = _find_default_sorald_jar()
OPTION_LIST_SEP = ","


def sorald(
    subcommand: str,
    *args,
    sorald_jar: pathlib.Path = DEFAULT_SORALD_JAR_PATH,
    timeout: Optional[int] = None,
    **kwargs,
) -> Tuple[int, bytes, bytes]:
    """Wrapper around the Sorald cli.

    Note that iterables (not including strings) passed as values are converted into
    comma-separated strings, as that's what Sorald currently uses.

    Args:
        subcommand: The subcommand to execute.
        args: Positional arguments. Each positional argument is converted into a string.
        sorald_jar: Path to the sorald jarfile.
        timeout: Amount of seconds before process should be aborted.
        kwargs: Keyword arguments. Each keyword argument ``some_key=value`` is converted
            into ``"--some-key value``, where ``value`` is first evaluated as a string.
    Returns:
        A tuple of (return_code, stdout, stderr)
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
    try:
        proc = subprocess.run(cmd, capture_output=True, timeout=timeout)
        return proc.returncode, proc.stdout, proc.stderr
    except subprocess.TimeoutExpired:
        return -1, b"", b""


def available_rule_keys(
    sorald_jar: pathlib.Path = DEFAULT_SORALD_JAR_PATH,
) -> List[str]:
    """
    Args:
        sorald_jar: Path to the Sorald jarfile.
    Returns:
        The available rule keys in the given jarfile.
    """
    rc, stdout, _ = sorald("repair", "--help")
    assert rc == 0
    output_lines = iter(stdout.decode(sys.getdefaultencoding()).split("\n"))

    dropped_initial_lines = itertools.dropwhile(
        lambda line: not line.strip().startswith("--rule-key"), output_lines
    )
    next(dropped_initial_lines)  # drop --rule-key line
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
