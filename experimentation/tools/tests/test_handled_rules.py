import pytest

from sorald import handled_rules
from _constants import MOCK_PROCESSORS


@pytest.mark.parametrize(
    "processor_path,expected",
    [
        (MOCK_PROCESSORS / "Processor.java", 1068),
        (MOCK_PROCESSORS / "IncompleteProcessor.java", 1444),
        (MOCK_PROCESSORS / "LineBreak.java", 2097),
        (MOCK_PROCESSORS / "Inverse.java", 2272),
    ],
)
def test_get_rule_key_should_return_key(processor_path, expected):
    assert handled_rules.get_rule_key(processor_path) == expected


@pytest.mark.parametrize(
    "heading_text,expected",
    [
        (
            "Unused local variables should be removed (Sonar Rule 1481)",
            "#unused-local-variables-should-be-removed-sonar-rule-1481",
        ),
        (
            '".equals()" should not be used to test the values of "Atomic" classes (Sonar Rule 2204)',
            "#equals-should-not-be-used-to-test-the-values-of-atomic-classes-sonar-rule-2204",
        ),
        (
            '"Collections.EMPTY_LIST", "EMPTY_MAP", and "EMPTY_SET" should not be used (Sonar Rule 1596)',
            "#collectionsempty_list-empty_map-and-empty_set-should-not-be-used-sonar-rule-1596",
        ),
    ],
)
def test_get_link_to_repair_description(heading_text, expected):
    assert handled_rules.get_link_to_repair_description(heading_text) == expected


@pytest.mark.parametrize(
    "processor_path,expected",
    [
        (
            MOCK_PROCESSORS / "Processor.java",
            'Unused "private" fields should be removed',
        ),
        (
            MOCK_PROCESSORS / "IncompleteProcessor.java",
            '"public static" fields should be constant',
        ),
        (
            MOCK_PROCESSORS / "LineBreak.java",
            '"equals(Object obj)" should test argument type',
        ),
        (
            MOCK_PROCESSORS / "Inverse.java",
            '"Iterator.next()" methods should throw "NoSuchElementException"',
        ),
    ],
)
def test_get_rule_title_should_return_title(processor_path, expected):
    assert handled_rules.get_rule_title(processor_path) == expected
