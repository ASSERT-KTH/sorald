package sorald.sonar;

import org.sonar.java.AnalyzerMessage;

/** Facade around {@link org.sonar.java.AnalyzerMessage} */
public class Bug {
    private final int lineNumber;
    private final String fileName;

    Bug(AnalyzerMessage message) {
        this.lineNumber = message.getLine();
        this.fileName = message.getInputComponent().key().replace(":", "");
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getFileName() {
        return fileName;
    }
}
