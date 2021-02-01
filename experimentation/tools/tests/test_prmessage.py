import shlex
import re
import pathlib

import pytest
import requests

from sorald import prmessage


@pytest.mark.parametrize("rule_key", [2111, 2204, 2142])
def test_generates_correct_message(capsys, rule_key):
    num_repairs = 342
    args = shlex.split(f"--rule-key {rule_key} --num-repairs {num_repairs}")

    prmessage.main(args)

    output = capsys.readouterr().out
    assert f"This PR fixes {num_repairs} violations of [Sonar Rule {rule_key}" in output
    urls = re.findall(r"(?<=\()https://.*?(?=\))", output)

    assert len(urls) == 3
    for url in urls:
        resp = requests.get(url)
        assert resp.status_code == 200


def test_uses_correct_sonar_version():
    """Check that the version of Sonar specified in the script is present in
    the pom.xml file of Sorald. The version number is sufficiently distinct
    that it's enough we just verify it's in there.
    """
    pom_content = (
        pathlib.Path(__file__).parent.parent.parent.parent / "pom.xml"
    ).read_text(encoding="utf8")
    assert re.findall(f"<version>{prmessage.SONAR_VERSION}</version>", pom_content)
