package sorald.sonar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.java.AnalyzerMessage;
import org.sonar.java.SonarComponents;
import org.sonar.java.ast.JavaAstScanner;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import org.sonar.java.model.VisitorsBridge;
import org.sonar.java.se.SymbolicExecutionMode;
import org.sonar.plugins.java.api.JavaFileScanner;

import static java.nio.charset.StandardCharsets.UTF_8;

/** Adapter class for interfacing with sonar-java's verification and analysis facilities. */
public class RuleVerifier {

    /**
     * Verify that the given file has at least one issue according to check.
     *
     * @param filename Path to a file.
     * @param check A Sonar check.
     */
    @SuppressWarnings("UnstableApiUsage")
    public static void verifyHasIssue(String filename, JavaFileScanner check) {
        JavaCheckVerifier.newVerifier().onFile(filename).withCheck(check).verifyIssues();
    }

    /**
     * Verify that all the given files have at least one issue according to check.
     *
     * @param filesToScan Paths to Java files.
     * @param check A Sonar check.
     */
    @SuppressWarnings("UnstableApiUsage")
    public static void verifyHasIssue(List<String> filesToScan, JavaFileScanner check) {
        filesToScan.forEach(filename -> verifyHasIssue(filename, check));
    }

    /**
     * Analyze the files with respect to check.
     *
     * @param filesToScan A list of paths to files.
     * @param baseDir The base directory of the current project.
     * @param check A Sonar check.
     * @return All messages produced by the analyzer, for all files.
     */
    @SuppressWarnings("UnstableApiUsage")
    public static Set<RuleViolation> analyze(List<String> filesToScan, File baseDir, JavaFileScanner check) {
        // must append a separator to the basedir string as Sonar appends the filenames directly to it ...
        String baseDirStr = baseDir.toString() + File.separator;
        List<InputFile> inputFiles =
                filesToScan.stream()
                        .map(filename -> toInputFile(baseDirStr, filename))
                        .collect(Collectors.toList());

        SoraldSonarComponents sonarComponents = createSonarComponents(baseDir);
        JavaAstScanner scanner = new JavaAstScanner(sonarComponents);
        VisitorsBridge visitorsBridge =
                new VisitorsBridge(
                        Collections.singletonList(check),
                        Collections.emptyList(),
                        sonarComponents,
                        SymbolicExecutionMode.ENABLED);
        scanner.setVisitorBridge(visitorsBridge);

        scanner.scan(inputFiles);

        return sonarComponents.getMessages().stream()
                .map(RuleViolation::new)
                .collect(Collectors.toSet());
    }

    /**
     * Verify that the file pointed to by filename does not violate the rule checked by check.
     *
     * @param filename Path to a file.
     * @param check A Sonar check.
     */
    @SuppressWarnings("UnstableApiUsage")
    public static void verifyNoIssue(String filename, JavaFileScanner check) {
        JavaCheckVerifier.newVerifier().onFile(filename).withCheck(check).verifyNoIssues();
    }

    private static InputFile toInputFile(String baseDir, String filename) {
        try {
            return new TestInputFileBuilder(baseDir, filename)
                    .setContents(new String(Files.readAllBytes(Paths.get(filename)), UTF_8))
                    .setCharset(UTF_8)
                    .setLanguage("java")
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("failed to read file " + filename);
        }
    }

    private static SoraldSonarComponents createSonarComponents(File baseDir) {
        SensorContextTester context = SensorContextTester.create(baseDir);
        SoraldSonarComponents sonarComponents = new SoraldSonarComponents(context.fileSystem());
        sonarComponents.setSensorContext(context);
        return sonarComponents;
    }

    private static class SoraldSonarComponents extends SonarComponents {
        private final Set<AnalyzerMessage> messages;

        public SoraldSonarComponents(DefaultFileSystem fs) {
            super(null, fs, null, null, null, null);
            messages = new HashSet<>();
        }

        @Override
        public void reportIssue(AnalyzerMessage analyzerMessage) {
            super.reportIssue(analyzerMessage);
            messages.add(analyzerMessage);
        }

        public Set<AnalyzerMessage> getMessages() {
            return Collections.unmodifiableSet(messages);
        }

        /*
         * The following methods simply override methods that use fields that we have not set values for
         */
    }
}
