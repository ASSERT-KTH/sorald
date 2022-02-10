import shlex
import re

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


@pytest.mark.skip(
    reason="Needs to be done after we put the SonarJava plugin URL in a config file"
)
def test_uses_correct_sonar_version():
    """Check that the version of Sonar Java plugin specified matches the version of
    downloaded plugin in sources. The version number is sufficiently
    distinct that it's enough we just verify it's in resources.
    """

    pass
