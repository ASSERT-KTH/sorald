package sorald.sonar;

/** Representation of a violation of some Sonar rule */
public interface RuleViolation {

    /** @return The line number related to the rule violation. */
    int getLineNumber();

    /** @return The name of the file that was analyzed. */
    String getFileName();

    /** @return The name of the check class that generated this warning. */
    String getCheckName();
}
