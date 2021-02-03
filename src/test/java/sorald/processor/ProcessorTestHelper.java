package sorald.processor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.sonar.java.checks.SynchronizationOnStringOrBoxedCheck;
import org.sonar.plugins.java.api.JavaFileScanner;
import sorald.Constants;
import sorald.Main;
import sorald.PrettyPrintingStrategy;
import sorald.sonar.Checks;
import sorald.sonar.RuleVerifier;

/** Helper functions for {@link ProcessorTest}. */
public class ProcessorTestHelper {
    public static final Path TEST_FILES_ROOT =
            Paths.get(Constants.PATH_TO_RESOURCES_FOLDER).resolve("processor_test_files");

    static final String EXPECTED_FILE_SUFFIX = ".expected";
    // The processors related to these checks currently cause problems with the sniper printer
    static final List<Class<?>> BROKEN_WITH_SNIPER =
            Arrays.asList(SynchronizationOnStringOrBoxedCheck.class);

    /**
     * Create a {@link ProcessorTestCase} from a non-compliant (according to SonarQube rules) Java
     * source file.
     *
     * <p>For this to work out, the directory that the Java file is located in must be prefixed with
     * "RULE_KEY_". For example, if the test file is for the rule S2164 (which is related to the
     * check {@link org.sonar.java.checks.MathOnFloatCheck}), the directory name must start with
     * "S2164_" or "2164_". The rest of the directory name doesn't matter, and the test file itself
     * can have any name as long as it ends with the .java file extension. Here's an example of a
     * compliant directory structure, where the Java files are test files for {@link
     * org.sonar.java.checks.MathOnFloatCheck}.
     *
     * <p>2164_MathOnFloat | ---- TestCaseFile.java | ---- OtherTestCaseFile.java
     *
     * @param nonCompliantFile Path to a non-compliant Java file that violates precisely one
     *     SonarQube rule.
     * @return A {@link ProcessorTestCase} for the given Java file.
     */
    @SuppressWarnings("unchecked")
    static <T extends JavaFileScanner> ProcessorTestCase<T> toProcessorTestCase(
            File nonCompliantFile) {
        File directory = nonCompliantFile.getParentFile();
        assert directory.isDirectory();
        String ruleKey = directory.getName().split("_")[0];
        Class<T> checkClass = (Class<T>) Checks.getCheck(ruleKey);
        String ruleName = checkClass.getSimpleName().replaceFirst("Check$", "");
        String outfileDirRelpath =
                parseSourceFilePackage(nonCompliantFile.toPath()).replace(".", File.separator);
        Path outfileRelpath = Paths.get(outfileDirRelpath).resolve(nonCompliantFile.getName());
        return new ProcessorTestCase<>(
                ruleName, ruleKey, nonCompliantFile, checkClass, outfileRelpath);
    }

    /**
     * Parse the package for a single Java source file. If there is no package statement in the
     * file, or the file cannot be read for any reason, an empty string is returned instead.
     */
    private static String parseSourceFilePackage(Path sourceFile) {
        List<String> lines;
        try {
            lines = Files.readAllLines(sourceFile);
        } catch (IOException e) {
            return "";
        }
        Pattern pattern = Pattern.compile("\\s*package\\s+?(\\S+)\\s*;");
        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                return matcher.group(1);
            }
        }
        return "";
    }

    /**
     * @param testFilesRoot Root of a test files directory.
     * @return A stream of {@link ProcessorTestCase}
     */
    public static Stream<ProcessorTestCase<?>> getTestCaseStream(File testFilesRoot) {
        return Arrays.stream(testFilesRoot.listFiles())
                .filter(File::isDirectory)
                .map(File::listFiles)
                .flatMap(Arrays::stream)
                .filter(file -> file.getName().endsWith(".java"))
                .filter(file -> !isIgnoredTestFile(file))
                .map(ProcessorTestHelper::toProcessorTestCase);
    }

    /**
     * @param testFile A test file.
     * @return Whether or not to ignore this file.
     */
    public static boolean isIgnoredTestFile(File testFile) {
        return testFile.getName().startsWith("IGNORE");
    }

    /**
     * @param testFile A test file.
     * @return Whether or not this file is standalone compilable.
     */
    public static boolean isStandaloneCompilableTestFile(File testFile) {
        return !testFile.getName().startsWith("NOCOMPILE");
    }

    /**
     * Return a stream of all valid test cases, based on the tests files in {@link
     * ProcessorTestHelper#TEST_FILES_ROOT}.
     */
    public static Stream<ProcessorTestCase<?>> getTestCaseStream() {
        return getTestCaseStream(TEST_FILES_ROOT.toFile());
    }

    /** Run sorald on the given test case. */
    public static void runSorald(ProcessorTestCase<?> testCase, String... extraArgs)
            throws Exception {
        RuleVerifier.verifyHasIssue(
                testCase.nonCompliantFile.getAbsolutePath(), testCase.createCheckInstance());
        runSorald(testCase.nonCompliantFile, testCase.checkClass, extraArgs);
    }

    /** Run sorald on the given file with the given checkClass * */
    public static void runSorald(
            File originaFilesPath, Class<? extends JavaFileScanner> checkClass, String... extraArgs)
            throws Exception {
        String originalFileAbspath = originaFilesPath.getAbsolutePath();

        boolean brokenWithSniper = BROKEN_WITH_SNIPER.contains(checkClass);
        var coreArgs =
                new String[] {
                    Constants.REPAIR_COMMAND_NAME,
                    Constants.ARG_ORIGINAL_FILES_PATH,
                    originalFileAbspath,
                    Constants.ARG_RULE_KEYS,
                    Checks.getRuleKey(checkClass),
                    Constants.ARG_WORKSPACE,
                    Constants.SORALD_WORKSPACE,
                    Constants.ARG_PRETTY_PRINTING_STRATEGY,
                    brokenWithSniper
                            ? PrettyPrintingStrategy.NORMAL.name()
                            : PrettyPrintingStrategy.SNIPER.name()
                };
        String[] allArgs =
                Stream.of(coreArgs, extraArgs).flatMap(Arrays::stream).toArray(String[]::new);
        Main.main(allArgs);
    }

    /**
     * A wrapper class to hold the information required to execute a test case for a single file and
     * rule with the associated processor.
     */
    public static class ProcessorTestCase<T extends JavaFileScanner> {
        public final String ruleName;
        public final String ruleKey;
        public final File nonCompliantFile;
        public final Class<T> checkClass;
        public final Path outfileRelpath;

        public ProcessorTestCase(
                String ruleName,
                String ruleKey,
                File nonCompliantFile,
                Class<T> checkClass,
                Path outfileRelpath) {
            this.ruleName = ruleName;
            this.ruleKey = ruleKey;
            this.nonCompliantFile = nonCompliantFile;
            this.checkClass = checkClass;
            this.outfileRelpath = outfileRelpath;
        }

        @Override
        public String toString() {
            return "ruleKey="
                    + ruleKey
                    + " ruleName="
                    + ruleName
                    + " source="
                    + TEST_FILES_ROOT.relativize(nonCompliantFile.toPath());
        }

        public T createCheckInstance()
                throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
                        InstantiationException {
            return checkClass.getConstructor().newInstance();
        }

        public Optional<File> expectedOutfile() {
            File expectedOutfile =
                    nonCompliantFile
                            .toPath()
                            .resolveSibling(nonCompliantFile.getName() + EXPECTED_FILE_SUFFIX)
                            .toFile();
            return Optional.ofNullable(expectedOutfile.isFile() ? expectedOutfile : null);
        }

        public Path repairedFilePath() {
            return Paths.get(Constants.SORALD_WORKSPACE)
                    .resolve(Constants.SPOONED)
                    .resolve(outfileRelpath);
        }
    }
}
