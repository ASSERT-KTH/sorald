import pathlib
import shlex
import re

import pytest
import requests

from sorald import prmessage
from sorald._helpers.sonar_metadata import SONAR_VERSION


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
    """Check that the version of Sonar Java plugin specified matches the
    version of downloaded plugin in sources. The version number is sufficiently
    distinct that it's enough we just verify it's in configuration file.
    """

    config = (
        pathlib.Path(__file__).parent.parent.parent.parent
        / "src"
        / "main"
        / "resources"
        / "config.properties"
    ).read_text(encoding="utf8")

    expected_url = (
        "https://repo1.maven.org/maven2/org/sonarsource/java/sonar-java-plugin/"
        f"{SONAR_VERSION}/sonar-java-plugin-{SONAR_VERSION}.jar"
    )

    assert expected_url in config
