package sorald.sonar;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.java.AnalyzerMessage;
import org.sonar.java.SonarComponents;
import org.sonar.java.ast.JavaAstScanner;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import org.sonar.java.model.JavaVersionImpl;
import org.sonar.java.model.VisitorsBridge;
import org.sonar.java.se.SymbolicExecutionMode;
import org.sonar.plugins.java.api.JavaFileScanner;

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
    public static Set<RuleViolation> analyze(
            List<String> filesToScan, File baseDir, JavaFileScanner check) {
        return analyze(filesToScan, baseDir, Collections.singletonList(check));
    }

    /**
     * Analyze the files with all of the provided checks.
     *
     * @param filesToScan A list of paths to files.
     * @param baseDir The base directory of the current project.
     * @param checks Sonar checks to use.
     * @return All messages produced by the analyzer, for all files and all checks.
     */
    @SuppressWarnings("UnstableApiUsage")
    public static Set<RuleViolation> analyze(
            List<String> filesToScan, File baseDir, List<? extends JavaFileScanner> checks) {
        List<InputFile> inputFiles =
                filesToScan.stream()
                        .map(filename -> toInputFile(baseDir, filename))
                        .collect(Collectors.toList());

        SoraldSonarComponents sonarComponents = createSonarComponents(baseDir);
        JavaAstScanner scanner = createAstScanner(sonarComponents, checks);

        scanner.scan(inputFiles);

        return sonarComponents.getMessages().stream()
                .map(ScannedViolation::new)
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

    private static InputFile toInputFile(File baseDir, String filename) {
        // must append a separator to the basedir string as Sonar appends the filenames directly to
        // it
        final String baseDirStr = baseDir.toString() + File.separator;
        try {
            return new TestInputFileBuilder(baseDirStr, filename)
                    .setContents(new String(Files.readAllBytes(Paths.get(filename)), UTF_8))
                    .setCharset(UTF_8)
                    .setLanguage("java")
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("failed to read file " + filename);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private static JavaAstScanner createAstScanner(
            SonarComponents sonarComponents, List<? extends JavaFileScanner> checks) {
        JavaAstScanner scanner = new JavaAstScanner(sonarComponents);
        VisitorsBridge visitorsBridge =
                new VisitorsBridge(
                        checks,
                        // TODO set the classpath to something reasonable
                        Collections.emptyList(),
                        sonarComponents,
                        SymbolicExecutionMode.getMode(checks.toArray(new JavaFileScanner[0])));
        // TODO set the version number to something appropriate for the current context
        //      setting it too high may yield false positives (fixes that aren't applicable to lower
        // versions)
        //      setting it too low may yield false negatives and parsing issues
        visitorsBridge.setJavaVersion(new JavaVersionImpl(14));
        scanner.setVisitorBridge(visitorsBridge);
        return scanner;
    }

    private static SoraldSonarComponents createSonarComponents(File baseDir) {
        // FIXME The SensorContextTester is an internal and unstable component in sonar,
        //       we should implement our own SensorContext
        SensorContextTester context = SensorContextTester.create(baseDir);
        context.setSettings(
                new MapSettings().setProperty(SonarComponents.FAIL_ON_EXCEPTION_KEY, true));
        SoraldSonarComponents sonarComponents = new SoraldSonarComponents(context.fileSystem());
        sonarComponents.setSensorContext(context);
        return sonarComponents;
    }

    /**
     * A simple subclass of SonarComponents that stores all analyzer messages. These are by default
     * stored in a storage container, but it seems easier for our use case to just intercept them.
     *
     * <p>This IS a bit of a hack, so it wouldn't be unreasonable to try to do this the "proper
     * way".
     */
    private static class SoraldSonarComponents extends SonarComponents {
        private final List<AnalyzerMessage> messages;

        public SoraldSonarComponents(DefaultFileSystem fs) {
            // FIXME I'm very unsure about supplying null for all of these constructor values.
            //       They should not be null, but at this time I don't know that to put there, and
            //       with our current usage this hack appears to work.
            super(null, fs, null, null, null, null);
            messages = new ArrayList<>();
        }

        @Override
        public void reportIssue(AnalyzerMessage analyzerMessage) {
            super.reportIssue(analyzerMessage);
            messages.add(analyzerMessage);
        }

        public List<AnalyzerMessage> getMessages() {
            return Collections.unmodifiableList(messages);
        }
    }
}
