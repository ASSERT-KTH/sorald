package sorald.sonar;

import sorald.rule.IRuleType;

/** Enumeration of Sonar rule types */
public enum SonarRuleType implements IRuleType {
    BUG("Bug"),
    VULNERABILITY("Vulnerability"),
    CODE_SMELL("Code_Smell"),
    SECURITY_HOTSPOT("Security_Hotspot");

    private final String name;

    SonarRuleType(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
