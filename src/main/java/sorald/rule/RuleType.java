package sorald.rule;

/** Enumeration of Sonar rule types */
public enum RuleType implements IRuleType {
    BUG("Bug"),
    VULNERABILITY("Vulnerability"),
    CODE_SMELL("Code_Smell"),
    SECURITY_HOTSPOT("Security_Hotspot");

    private final String name;

    RuleType(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
