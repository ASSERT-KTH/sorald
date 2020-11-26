package sorald.sonar;

import java.util.Objects;
import org.sonar.java.AnalyzerMessage;

/** Facade around {@link org.sonar.java.AnalyzerMessage} */
public class RuleViolation {
    private final AnalyzerMessage message;

    RuleViolation(AnalyzerMessage message) {
        this.message = message;
    }

    /** @return The line number related to the rule violation. */
    public int getLineNumber() {
        return Objects.requireNonNull(message.getLine());
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
}
