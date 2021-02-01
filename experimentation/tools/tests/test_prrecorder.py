"""Test cases for the prrecorder. Run with pytest."""
import pathlib
import sys
import json
import datetime
import shutil
import shlex

import pytest

from sorald import prrecorder
from sorald._helpers import jsonkeys

import _constants

STATS_JSON = _constants.RESOURCES_DIR / "stats.json"
PRS_JSON_FINAL = _constants.RESOURCES_DIR / "prs_final.json"

OWNER = "redhat-developer"
REPO = "rsp-server"
PR_NUMBER = 619
RECORD_ID = prrecorder.create_record_id(OWNER, REPO, PR_NUMBER)


def test_make_full_record(tmp_path):
    # arrange
    prs_json_file = tmp_path / "prs.json"

    record_initial_args = shlex.split(
        f"-o {OWNER} -r {REPO} -p {PR_NUMBER} -f {prs_json_file} -s {STATS_JSON}"
    )
    record_final_args = record_initial_args[:-2]

    # act
    prrecorder.run_cli([prrecorder.RECORD_INITIAL] + record_initial_args)
    prrecorder.run_cli([prrecorder.RECORD_FINAL] + record_final_args)

    # assert
    expected_record = prrecorder.read_json_if_exists(
        PRS_JSON_FINAL, prrecorder.ENCODING
    )[RECORD_ID]
    actual_record = prrecorder.read_json_if_exists(prs_json_file, prrecorder.ENCODING)[
        RECORD_ID
    ]

    # must remove the record metadata section as it will always differ due to time
    del actual_record[jsonkeys.RECORD.SECTION_KEY]
    del expected_record[jsonkeys.RECORD.SECTION_KEY]

    assert actual_record == expected_record


def test_add_manual_edit(tmp_path):
    # arrange
    prs_json_file = tmp_path / "prs.json"
    shutil.copy(PRS_JSON_FINAL, prs_json_file)

    diff_text = "hello there :)"
    diff_file = tmp_path / "diff.txt"
    diff_file.write_text(diff_text, encoding=sys.getdefaultencoding())

    edit_reason = "It felt like the right thing to do"
    edit_type = "beforeOpenPr"

    args = shlex.split(
        f"add-manual-edit -o {OWNER} -r {REPO} -p {PR_NUMBER} "
        f"-e '{edit_reason}' -t {edit_type} -d {diff_file} -f {prs_json_file}"
    )

    # act
    prrecorder.run_cli(args)

    # assert
    record = prrecorder.read_json_if_exists(prs_json_file, prrecorder.ENCODING)[
        RECORD_ID
    ]
    manual_edits = record[jsonkeys.MANUAL_EDITS.SECTION_KEY]
    assert len(manual_edits) == 1
    assert sorted(manual_edits[0].values()) == sorted(
        [diff_text, edit_reason, edit_type]
    )
