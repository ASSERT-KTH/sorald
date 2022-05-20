package sorald.rule;

/** A static analysis rule */
public interface Rule {

    /** @return A key that uniquely identifies this rule within Sorald. */
    String getKey();

    /** @return The name of this rule. */
    String getName();

    /** @return The type of this rule. */
    IRuleType getType();
}
