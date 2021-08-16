package sorald.sonar;

import java.nio.file.Path;
import org.sonar.api.batch.fs.InputComponent;
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
        return extractPath(message.getInputComponent()).toAbsolutePath().normalize();
    }

    private static Path extractPath(InputComponent inputComponent) {
        // This key is on the form /path/to/:File.java (note the colon after the final slash).
        // We can't however blindly remove all colons as that destroys Windows filepaths (e.g.
        // C:\path\to\:File.java).
        String key = inputComponent.key();
        int indexOfLastColon = key.lastIndexOf(':');
        String rawPath = key.substring(0, indexOfLastColon) + key.substring(indexOfLastColon + 1);
        return Path.of(rawPath);
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
