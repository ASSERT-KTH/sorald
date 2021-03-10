import shlex

import pytest

import sorald.benchmark


COMMITS_CSV_CONTENT = """url,commit
https://github.com/spoonlabs/sorald.git,5e64028cd82336b34a68c60d314ce1c57b4bf276
"""


def test_run_benchmark(tmp_path):
    """This is just a sanity check that the benchmark is still working."""
    commits_csv = tmp_path / "commits.csv"
    commits_csv.write_text(COMMITS_CSV_CONTENT, encoding="utf8")
    output = tmp_path / "out.csv"

    try:
        sorald.benchmark.main(
            shlex.split(
                f"--commits-csv {commits_csv} -o {output} --rule-keys 1854 2116"
            )
        )
    except SystemExit:
        pytest.fail()

    assert output.is_file()
