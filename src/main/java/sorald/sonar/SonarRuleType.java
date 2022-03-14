package sorald.sonar;

import sorald.rule.RuleType;

/** Enumeration of Sonar rule types */
public enum SonarRuleType implements RuleType {
    BUG,
    VULNERABILITY,
    CODE_SMELL,
    SECURITY_HOTSPOT
}
