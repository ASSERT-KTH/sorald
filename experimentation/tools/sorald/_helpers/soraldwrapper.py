"""Python wrapper around the Sorald CLI."""
import subprocess
import itertools
import pathlib


DEFAULT_SORALD_JAR_PATH = (
    pathlib.Path(__file__).absolute().parent.parent.parent.parent.parent
    / "target"
    / "sorald-1.1-SNAPSHOT-jar-with-dependencies.jar"
).resolve(strict=False)


def sorald(
    subcommand: str, *args, sorald_jar: pathlib.Path = DEFAULT_SORALD_JAR_PATH, **kwargs
):
    """Wrapper around the Sorald cli.

    Args:
        subcommand: The subcommand to execute.
        args: Positional arguments. Each positional argument is converted into a string.
        sorald_jar: Path to the sorald jarfile.
        kwargs: Keyword arguments. Each keyword argument ``some_key=value`` is
            converted into ``"--some-key value``, where ``value`` is first
            evaluated as a string..
    """
    cmd = [
        "java",
        "-jar",
        str(sorald_jar),
        subcommand,
        *list(map(str, args)),
        *list(
            itertools.chain.from_iterable(
                [
                    (f"--{key.replace('_', '-')}", str(value))
                    for key, value in kwargs.items()
                ]
            )
        ),
    ]
    subprocess.run(cmd)
