package sorald.sonar;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.sonar.java.AnalyzerMessage;
import org.sonar.plugins.java.api.JavaCheck;
import sorald.rule.RuleViolation;

/** Facade around {@link org.sonar.java.AnalyzerMessage} */
class ScannedViolation extends RuleViolation {
    private final AnalyzerMessage message;
    private final AnalyzerMessage.TextSpan primaryLocation;

    ScannedViolation(AnalyzerMessage message) {
        if (message.primaryLocation() == null) {
            throw new IllegalArgumentException(
                    "message for '"
                            + getCheckName(message.getCheck())
                            + "' lacks primary location");
        }
        this.message = message;
        this.primaryLocation = message.primaryLocation();
    }

    @Override
    public int getStartLine() {
        return primaryLocation.startLine;
    }

    @Override
    public int getEndLine() {
        return primaryLocation.endLine;
    }

    @Override
    public int getStartCol() {
        return primaryLocation.startCharacter;
    }

    @Override
    public int getEndCol() {
        return primaryLocation.endCharacter;
    }

    @Override
    public Path getAbsolutePath() {
        return Paths.get(message.getInputComponent().key().replace(":", ""))
                .toAbsolutePath()
                .normalize();
    }

    @Override
    public String getRuleKey() {
        return Checks.getRuleKey(message.getCheck().getClass());
    }

    @Override
    public String getMessage() {
        return message.getMessage();
    }

    private static String getCheckName(JavaCheck check) {
        return check.getClass().getSimpleName();
    }
}
