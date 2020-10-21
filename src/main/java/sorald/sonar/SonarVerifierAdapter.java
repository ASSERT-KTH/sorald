package sorald.sonar;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.sonar.java.AnalyzerMessage;
import org.sonar.java.checks.verifier.MultipleFilesJavaCheckVerifier;
import org.sonar.plugins.java.api.JavaFileScanner;

/** Adapter class for interfacing with sonar-java's verification and analysis facilities. */
public class SonarVerifierAdapter {

    /**
     * Verify that the given file has at least one issue according to check.
     *
     * @param filename Path to a file.
     * @param check A Sonar check.
     */
    @SuppressWarnings("UnstableApiUsage")
    public static void verifyHasIssue(String filename, JavaFileScanner check) {
        verifyHasIssue(Arrays.asList(filename), check);
    }

    /**
     * Verify that all the given files have at least one issue according to check.
     *
     * @param filesToScan Paths to Java files.
     * @param check A Sonar check.
     */
    @SuppressWarnings("UnstableApiUsage")
    public static void verifyHasIssue(List<String> filesToScan, JavaFileScanner check) {
        MultipleFilesJavaCheckVerifier.verify(filesToScan, check, true);
    }

    /**
     * Analyze the file with respect to check.
     *
     * @param filename Path to a file.
     * @param check A Sonar check.
     * @return All messages produced by the analyzer.
     */
    @SuppressWarnings("UnstableApiUsage")
    public static Set<AnalyzerMessage> analyze(String filename, JavaFileScanner check) {
        return analyze(Arrays.asList(filename), check);
    }

    /**
     * Analyze the files with respect to check.
     *
     * @param filesToScan A list of paths to files.
     * @param check A Sonar check.
     * @return All messages produced by the analyzer, for all files.
     */
    @SuppressWarnings("UnstableApiUsage")
    public static Set<AnalyzerMessage> analyze(List<String> filesToScan, JavaFileScanner check) {
        return MultipleFilesJavaCheckVerifier.verify(filesToScan, check, false);
    }

    /**
     * Verify that the file pointed to by filename does not violate the rule checked by check.
     *
     * @param filename Path to a file.
     * @param check A Sonar check.
     */
    @SuppressWarnings("UnstableApiUsage")
    public static void verifyNoIssue(String filename, JavaFileScanner check) {
        MultipleFilesJavaCheckVerifier.verifyNoIssue(Arrays.asList(filename), check);
    }
}
