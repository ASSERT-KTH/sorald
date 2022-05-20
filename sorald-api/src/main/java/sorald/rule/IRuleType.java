package sorald.rule;

public interface IRuleType {

    /**
     * Returns the name of the rule type. A rule type is a category of rules, such as "Bug",
     * "Vulnerability".
     */
    String getName();
}
