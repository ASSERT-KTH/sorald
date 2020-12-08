package sorald.sonar;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

/** Representation of a violation of some Sonar rule */
public abstract class RuleViolation implements Comparable<RuleViolation> {

    /** @return The line the element that violates the rule starts on. */
    public abstract int getStartLine();

    /** @return The line the element that violates the rule ends on. */
    public abstract int getEndLine();

    /** @return The column the element that violates the rule starts on. */
    public abstract int getStartCol();

    /** @return The column the element that violates the rule ends on. */
    public abstract int getEndCol();

    /** @return The name of the file that was analyzed. */
    public abstract String getFileName();

    /** @return The name of the check class that generated this warning. */
    public abstract String getCheckName();

    /** @return The key of the violated rule. */
    public abstract String getRuleKey();

    /**
     * @param rootDir The root directory of the current project. If it is a file system root, the
     *     violation ID gets an absolute filepath.
     * @return A violation ID that is unique relative to the given root directory.
     */
    public String violationId(Path rootDir) {
        boolean rootDirIsFsRoot =
                Arrays.stream(File.listRoots()).map(File::toPath).anyMatch(rootDir::equals);
        Path absPath = Paths.get(getFileName());
        Path idPath = rootDirIsFsRoot ? absPath : rootDir.relativize(absPath);
        return String.format(
                "%s:%s:%s:%s:%s:%s",
                getRuleKey(), idPath, getStartLine(), getStartCol(), getEndLine(), getEndCol());
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
