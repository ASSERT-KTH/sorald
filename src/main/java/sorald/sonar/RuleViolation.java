package sorald.sonar;

import java.util.Objects;

/** Representation of a violation of some Sonar rule */
public abstract class RuleViolation {

    /** @return The line number related to the rule violation. */
    public abstract int getLineNumber();

    /** @return The name of the file that was analyzed. */
    public abstract String getFileName();

    /** @return The name of the check class that generated this warning. */
    public abstract String getCheckName();

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RuleViolation)) {
            return false;
        }
        var other = (RuleViolation) obj;
        return getLineNumber() == other.getLineNumber()
                && getFileName().equals(other.getFileName())
                && getCheckName().equals(other.getCheckName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLineNumber(), getFileName(), getCheckName());
    }
}
