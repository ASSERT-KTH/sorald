import shlex
import re
import pathlib

import pytest
import requests

from sorald import prmessage
from sorald._helpers import sonar_metadata


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
    """Check that the version of Sonar Java specified matches the version of
    loaded plugin in src/main/resources. The version number is sufficiently
    distinct that it's enough we just verify it's in resources.
    """

    path_to_main_resources = (
        pathlib.Path(__file__).parent.parent.parent.parent / "src/main/resources"
    )
    sonar_java_jars = list(path_to_main_resources.glob("sonar-java-plugin-*.jar"))

    # We should have precisely one sonar-java plugin
    assert len(sonar_java_jars) == 1

    sonar_java = sonar_java_jars[0].name
    assert re.match(f"sonar-java-plugin-{sonar_metadata.SONAR_VERSION}.jar", sonar_java)
