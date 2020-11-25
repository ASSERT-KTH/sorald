package sorald.sonar;

import org.sonar.java.AnalyzerMessage;

import java.util.Objects;

/** Facade around {@link org.sonar.java.AnalyzerMessage} */
public class ScannedViolation extends RuleViolation {
    private final AnalyzerMessage message;

    ScannedViolation(AnalyzerMessage message) {
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
}
