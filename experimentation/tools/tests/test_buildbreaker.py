"""Tests for the buildbreaker module."""
import pathlib
import shlex
import shutil

import sorald.buildbreaker
from sorald._helpers import soraldwrapper

SORALD_PROCESSOR_TEST_FILES = (
    pathlib.Path(__file__).parent.parent.parent.parent
    / "src"
    / "test"
    / "resources"
    / "processor_test_files"
)


def test_non_zero_exit_on_repairable_violations():
    test_file = (
        SORALD_PROCESSOR_TEST_FILES
        / "1854_DeadStore"
        / "DeadInitializerInFlatBlock.java"
    )

    exit_status = sorald.buildbreaker.run(["--source", str(test_file)])

    assert exit_status != 0

def test_zero_exit_on_no_repairable_violations(tmp_path):
    """Test that a file in which all violations have been repaired causes a
    zero exit for the buildbreaker.
    """
    # arrange
    test_file = (
        SORALD_PROCESSOR_TEST_FILES
        / "1854_DeadStore"
        / "DeadInitializerInFlatBlock.java"
    )
    test_file_copy = tmp_path / test_file.name
    shutil.copy(test_file, test_file_copy)

    # repair all violations of rule 1854 (there should be no violations of any
    # other rules in the test file)
    soraldwrapper.sorald("repair", rule_key="1854", source=test_file_copy)

    # act
    exit_status = sorald.buildbreaker.run(["--source", str(test_file_copy)])

    # assert
    assert exit_status == 0
