package sorald.qodana;

import com.contrastsecurity.sarif.Region;
import com.contrastsecurity.sarif.Result;
import java.io.File;
import java.nio.file.Path;
import sorald.rule.RuleViolation;

public class QodanaRuleViolation extends RuleViolation {

    private int startLine;
    private int endLine;
    private int startCol;
    private int endCol;
    private Path filePath;
    private String ruleId;

    public QodanaRuleViolation(Result result, File projectRoot) {
        Region region = result.getLocations().get(0).getPhysicalLocation().getRegion();
        // in the sarif report endline and endcolum are nullable, we have to fix this here.
        startLine = region.getStartLine();
        endLine = (region.getEndLine() != null) ? region.getEndLine() : startLine;
        startCol = region.getStartColumn();
        endCol = (region.getEndColumn() != null) ? region.getEndColumn() : startCol;
        ruleId = result.getRuleId();
        filePath =
                Path.of(
                        projectRoot.getAbsolutePath(),
                        result.getLocations()
                                .get(0)
                                .getPhysicalLocation()
                                .getArtifactLocation()
                                .getUri());
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
    public Path getAbsolutePath() {
        return filePath;
    }

    @Override
    public String getRuleKey() {
        return ruleId;
    }
}
