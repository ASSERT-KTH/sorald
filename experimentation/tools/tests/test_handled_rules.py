import pytest

from sorald import handled_rules


@pytest.mark.parametrize(
    "title,rule_key,expected",
    [
        (
            "Unused local variables should be removed",
            1481,
            "#unused-local-variables-should-be-removed-sonar-rule-1481",
        ),
        (
            "\".equals()\" should not be used to test the values of \"Atomic\" classes",
            2204,
            "#equals-should-not-be-used-to-test-the-values-of-atomic-classes-sonar-rule-2204"
        ),
        (
            "\"Collections.EMPTY_LIST\", \"EMPTY_MAP\", and \"EMPTY_SET\" should not be used",
            1596,
            "#collectionsempty_list-empty_map-and-empty_set-should-not-be-used-sonar-rule-1596"
        )
    ]
)
def test_get_link_to_detail(title, rule_key, expected):
    assert handled_rules.get_link_to_detail(title, rule_key) == expected
