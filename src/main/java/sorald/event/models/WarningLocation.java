package sorald.event.models;

import sorald.Constants;
import sorald.sonar.RuleViolation;

public class WarningLocation {
    private final String filePath;
    private final Integer startLine;
    private final Integer endLine;
    private final Integer startColumn;
    private final Integer endColumn;
    private final String usableLocation;

    public WarningLocation(RuleViolation violation) {
        this.filePath = violation.getFileName();
        this.startLine = violation.getStartLine();
        this.endLine = violation.getEndLine();
        this.startColumn = violation.getStartCol();
        this.endColumn = violation.getEndCol();

        this.usableLocation =
                violation.getRuleKey()
                        + Constants.VIOLATION_USABLE_PATH_SEPARATOR
                        + filePath
                        + Constants.VIOLATION_USABLE_PATH_SEPARATOR
                        + startLine
                        + Constants.VIOLATION_USABLE_PATH_SEPARATOR
                        + endLine
                        + Constants.VIOLATION_USABLE_PATH_SEPARATOR
                        + startColumn
                        + Constants.VIOLATION_USABLE_PATH_SEPARATOR
                        + endColumn;
    }

    public String getFilePath() {
        return filePath;
    }

    public Integer getStartLine() {
        return startLine;
    }

    public Integer getEndLine() {
        return endLine;
    }

    public Integer getStartColumn() {
        return startColumn;
    }

    public Integer getEndColumn() {
        return endColumn;
    }

    public String getUsableLocation() {
        return usableLocation;
    }
}
