package sorald.cli;

import sorald.sonar.Checks;
import sorald.sonar.RuleViolation;

/** Rule violation specified from the CLI. */
class SpecifiedViolation extends RuleViolation {
    private final String ruleKey;
    private final String checkName;
    private final String fileName;
    private final int startLine;
    private final int startCol;
    private final int endLine;
    private final int endCol;

    SpecifiedViolation(
            String ruleKey, String fileName, int startLine, int startCol, int endLine, int endCol) {
        this.ruleKey = ruleKey;
        checkName = Checks.getCheck(ruleKey).getSimpleName();
        this.fileName = fileName;
        this.startLine = startLine;
        this.endLine = endLine;
        this.startCol = startCol;
        this.endCol = endCol;
    }

    @Override
    public int getStartLine() {
        return startLine;
    }

    @Override
    public int getEndLine() {
        return endLine;
    }

    @Override
    public int getStartCol() {
        return startCol;
    }

    @Override
    public int getEndCol() {
        return endCol;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String getCheckName() {
        return checkName;
    }

    @Override
    public String getRuleKey() {
        return ruleKey;
    }
}
