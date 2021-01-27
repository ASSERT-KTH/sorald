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
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.java.AnalyzerMessage;
import org.sonar.java.DefaultJavaResourceLocator;
import org.sonar.java.JavaClasspath;
import org.sonar.java.JavaSonarLintClasspath;
import org.sonar.java.JavaSquid;
import org.sonar.java.JavaTestClasspath;
import org.sonar.java.Measurer;
import org.sonar.java.SonarComponents;
import org.sonar.java.checks.CheckList;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import org.sonar.java.filters.PostAnalysisIssueFilter;
import org.sonar.java.model.JavaVersionImpl;
import org.sonar.plugins.java.api.JavaFileScanner;
import sorald.Constants;

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
        SoraldSonarComponents components = createSonarComponents(baseDir, checks);

        components.registerCheckClasses(CheckList.REPOSITORY_KEY, CheckList.getJavaChecks());
        components.registerTestCheckClasses(
                CheckList.REPOSITORY_KEY, CheckList.getJavaTestChecks());

        scanFiles(
                filesToScan.stream().map(f -> toInputFile(baseDir, f)).collect(Collectors.toList()),
                components);

        return components.getMessages().stream()
                .filter(message -> message.primaryLocation() != null)
                .map(ScannedViolation::new)
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void scanFiles(List<InputFile> sourceFiles, SoraldSonarComponents components) {
        Measurer measurer = new Measurer(components.getContext(), new NoSonarFilter());
        JavaSquid squid =
                new JavaSquid(
                        // TODO set the source version dynamically
                        JavaVersionImpl.fromString(Constants.DEFAULT_COMPLIANCE_LEVEL.toString()),
                        components,
                        measurer,
                        new DefaultJavaResourceLocator(components.getClasspath()),
                        new PostAnalysisIssueFilter(),
                        components.checkClasses());
        squid.scan(
                sourceFiles,
                List.of(), // TODO provide test files
                Collections.emptyList() // TODO provide generated files
                );
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

    private static SoraldSonarComponents createSonarComponents(
            File baseDir, List<? extends JavaFileScanner> checks) {
        var activeRulesBuilder = new ActiveRulesBuilder();
        checks.stream()
                .map(check -> Checks.toSonarRuleKey(Checks.getRuleKey(check.getClass())))
                .map(
                        ruleKey ->
                                new NewActiveRule.Builder()
                                        .setRuleKey(RuleKey.of("java", ruleKey))
                                        .setLanguage("java")
                                        .build())
                .forEach(activeRulesBuilder::addRule);

        CheckFactory checkFactory = new CheckFactory(activeRulesBuilder.build());

        // FIXME The SensorContextTester is an internal and unstable component in sonar,
        //       we should implement our own SensorContext
        SensorContextTester sensorContext = SensorContextTester.create(baseDir);

        DefaultFileSystem fs = sensorContext.fileSystem();
        // FIXME populate the classpaths
        var cp = new JavaSonarLintClasspath(new MapSettings().asConfig(), fs);
        var testCp = new JavaTestClasspath(new MapSettings().asConfig(), fs);

        SoraldSonarComponents sonarComponents =
                new SoraldSonarComponents(sensorContext.fileSystem(), cp, testCp, checkFactory);
        sonarComponents.setSensorContext(sensorContext);
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
        private final PostAnalysisIssueFilter postFilter;
        private final JavaClasspath cp;
        private final JavaTestClasspath testCp;
        private SensorContext context;

        public SoraldSonarComponents(
                DefaultFileSystem fs,
                JavaClasspath cp,
                JavaTestClasspath testCp,
                CheckFactory checkFactory) {
            this(fs, cp, testCp, checkFactory, new PostAnalysisIssueFilter());
        }

        public SoraldSonarComponents(
                DefaultFileSystem fs,
                JavaClasspath cp,
                JavaTestClasspath testCp,
                CheckFactory checkFactory,
                PostAnalysisIssueFilter postFilter) {
            super(new SoraldFileLinesContextFactory(), fs, cp, testCp, checkFactory, postFilter);
            messages = new ArrayList<>();
            this.postFilter = postFilter;
            this.cp = cp;
            this.testCp = testCp;
        }

        @Override
        public void reportIssue(AnalyzerMessage analyzerMessage) {
            super.reportIssue(analyzerMessage);
            messages.add(analyzerMessage);
        }

        @Override
        public void setSensorContext(SensorContext context) {
            this.context = context;
            super.setSensorContext(context);
        }

        public SensorContext getContext() {
            return context;
        }

        public List<AnalyzerMessage> getMessages() {
            return messages.stream().filter(this::shouldBeReported).collect(Collectors.toList());
        }

        public JavaClasspath getClasspath() {
            return cp;
        }

        private boolean shouldBeReported(AnalyzerMessage message) {
            return postFilter.accept(getRuleKey(message), message) && !fromNosonarLine(message);
        }

        private static boolean fromNosonarLine(AnalyzerMessage message) {
            return message.getLine() != null
                    && message.getInputComponent() instanceof DefaultInputFile
                    && ((DefaultInputFile) message.getInputComponent())
                            .hasNoSonarAt(message.getLine());
        }

        private static RuleKey getRuleKey(AnalyzerMessage message) {
            return RuleKey.of(
                    "java",
                    Checks.toSonarRuleKey(Checks.getRuleKey(message.getCheck().getClass())));
        }

        private static class SoraldFileLinesContextFactory implements FileLinesContextFactory {

            @Override
            public FileLinesContext createFor(InputFile inputFile) {
                return new SoraldFileLinesContext();
            }

            private static class SoraldFileLinesContext implements FileLinesContext {

                @Override
                public void setIntValue(String metricKey, int line, int value) {}

                @Override
                public void setStringValue(String metricKey, int line, String value) {}

                @Override
                public void save() {}
            }
        }
    }
}
