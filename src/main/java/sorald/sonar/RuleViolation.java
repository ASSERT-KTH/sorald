package sorald.sonar;

import java.util.Objects;

/** Representation of a violation of some Sonar rule */
public abstract class RuleViolation {

    /** @return The line the element that violates the rule starts on. */
    public abstract int getStartLine();

    /** @return The line the element that violates the rule ends on. */
    public abstract int getEndLine();

    /** @return The column the element that violates the rule starts on. */
    public abstract int getStartCol();

    /** @return The column the element that violates the rule ends on. */
    public abstract int getEndCol();

    /** @return The name of the file that was analyzed. */
    public abstract String getFileName();

    /** @return The name of the check class that generated this warning. */
    public abstract String getCheckName();

    /** @return The key of the violated rule. */
    public abstract String getRuleKey();

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RuleViolation)) {
            return false;
        }
        var other = (RuleViolation) obj;
        return getFileName().equals(other.getFileName())
                && getCheckName().equals(other.getCheckName())
                && getRuleKey().equals(other.getRuleKey())
                && getStartLine() == other.getStartLine()
                && getEndLine() == other.getEndLine()
                && getStartCol() == other.getStartCol()
                && getEndCol() == other.getEndCol();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getFileName(),
                getCheckName(),
                getRuleKey(),
                getStartLine(),
                getEndLine(),
                getStartCol(),
                getEndCol());
    }
}
