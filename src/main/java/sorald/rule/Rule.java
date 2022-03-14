package sorald.rule;

/**
 * A static analysis rule.
 *
 * @param <T> variant of static analyzer
 */
public interface Rule<T extends RuleType> {

    /** @return A key that uniquely identifies this rule within Sorald. */
    String getKey();

    /** @return The name of this rule. */
    String getName();

    /** @return The type of this rule. */
    T getType();
}
