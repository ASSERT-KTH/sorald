package sorald.sonar;

import org.sonar.java.AnalyzerMessage;

import java.util.Objects;

/** Facade around {@link org.sonar.java.AnalyzerMessage} */
public class Bug {
    private final AnalyzerMessage message;

    Bug(AnalyzerMessage message) {
        this.message = message;
    }

    public int getLineNumber() {
        return Objects.requireNonNull(message.getLine());
    }

    public String getFileName() {
        return message.getInputComponent().key().replace(":", "");
    }
}
