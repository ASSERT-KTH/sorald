import json
import shlex
import pathlib

import achievements

CUR_DIR = pathlib.Path(__file__).parent
PR_RECORDER_RESOURCES = CUR_DIR.parent / "pr_recorder" / "resources"
PRS_JSON_FINAL = PR_RECORDER_RESOURCES / "prs_final.json"


def test_correctly_renders_merged_pr(tmp_path):
    output_file = tmp_path / "ACHIEVEMENTS.md"
    args = shlex.split(
        f"{achievements.PRS_JSON_ARG} {PRS_JSON_FINAL} "
        f"{achievements.OUTPUT_ARG} {output_file}"
    )

    achievements.main(args)

    rendered_content = output_file.read_text(achievements.ENCODING)
    assert (
        rendered_content.strip()
        == """
# Achievements
This document presents an overview of the pull requests performed with Sorald.

## [redhat-developer/rsp-server#619](https://github.com/redhat-developer/rsp-server/pull/619)
This PR was opened at 2020-11-25 12:01:06 and merged at 2020-11-30 20:45:38.
The patch was generated fully automatically with Sorald.

It provided the following repairs:

* [Rule 2755](https://rules.sonarsource.com/java/RSPEC-2755)
    - Number of violations found: 4
    - Number of violations repaired: 4""".strip()
    )


def test_correctly_renders_pr_without_repair_data(tmp_path):
    # arrange
    output_file = tmp_path / "ACHIEVEMENTS.md"
    prs_json_file = tmp_path / "prs.json"

    prs_data = json.loads(PRS_JSON_FINAL.read_text(achievements.ENCODING))
    prs_data["redhat-developer/rsp-server#619"]["sorald_statistics"]["repairs"] = []
    prs_json_file.write_text(
        json.dumps(prs_data, indent=4), encoding=achievements.ENCODING
    )
    args = shlex.split(
        f"{achievements.PRS_JSON_ARG} {prs_json_file} "
        f"{achievements.OUTPUT_ARG} {output_file}"
    )

    # act
    achievements.main(args)

    # assert
    rendered_content = output_file.read_text(achievements.ENCODING)
    assert (
        rendered_content.strip()
        == """
# Achievements
This document presents an overview of the pull requests performed with Sorald.

## [redhat-developer/rsp-server#619](https://github.com/redhat-developer/rsp-server/ulls/619)
This PR was opened at 2020-11-25 12:01:06 and merged at 2020-11-30 20:45:38.
The patch was generated fully automatically with Sorald.

Detailed repair information is missing for this PR.""".strip()
    )
