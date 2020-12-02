package sorald.sonar;

import java.util.Objects;
import java.util.stream.Stream;
import org.sonar.java.AnalyzerMessage;

/** Facade around {@link org.sonar.java.AnalyzerMessage} */
public class RuleViolation implements Comparable<RuleViolation> {
    private final AnalyzerMessage message;

    RuleViolation(AnalyzerMessage message) {
        this.message = message;
    }

    /** @return The line the element that violates the rule starts on. */
    public int getStartLine() {
        return message.primaryLocation().startLine;
    }

    /** @return The line the element that violates the rule ends on. */
    public int getEndLine() {
        return message.primaryLocation().endLine;
    }

    /** @return The column the element that violates the rule starts on. */
    public int getStartCol() {
        return message.primaryLocation().startCharacter;
    }

    /** @return The column the element that violates the rule ends on. */
    public int getEndCol() {
        return message.primaryLocation().endCharacter;
    }

    /** @return The name of the file that was analyzed. */
    public String getFileName() {
        return message.getInputComponent().key().replace(":", "");
    }

    /** @return The name of the check class that generated this warning. */
    public String getCheckName() {
        return message.getCheck().getClass().getSimpleName();
    }

    /** @return The key of the violated rule. */
    public String getRuleKey() {
        return Checks.getRuleKey(message.getCheck().getClass());
    }

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

    @Override
    public int compareTo(RuleViolation violation) {
        int fileCmp = getFileName().compareTo(violation.getFileName());
        int ruleCmp = getRuleKey().compareTo(violation.getRuleKey());
        int startLineCmp = Integer.compare(getStartLine(), violation.getStartLine());
        int startColCmp = Integer.compare(getStartCol(), violation.getStartCol());
        int endLineCmp = Integer.compare(getEndCol(), violation.getEndCol());
        int endColCmp = Integer.compare(getEndCol(), violation.getEndCol());

        return Stream.of(fileCmp, ruleCmp, startLineCmp, startColCmp, endLineCmp, endColCmp)
                .filter(i -> i != 0)
                .findFirst()
                .orElse(0);
    }
}
