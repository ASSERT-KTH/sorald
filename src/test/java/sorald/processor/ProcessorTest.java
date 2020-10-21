package sorald.processor;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.sonar.java.checks.InterruptedExceptionCheck;
import org.sonar.java.checks.SynchronizationOnStringOrBoxedCheck;
import org.sonar.java.checks.serialization.SerializableFieldInSerializableClassCheck;
import org.sonar.plugins.java.api.JavaFileScanner;
import sorald.Constants;
import sorald.Main;
import sorald.PrettyPrintingStrategy;
import sorald.TestHelper;
import sorald.sonar.Verifier;

public class ProcessorTest {

    // The processors related to these checks currently cause problems with the sniper printer
    private static final List<Class<?>> BROKEN_WITH_SNIPER =
            Arrays.asList(
                    SynchronizationOnStringOrBoxedCheck.class,
                    InterruptedExceptionCheck.class,
                    SerializableFieldInSerializableClassCheck.class);

    /**
     * Parameterized test that processes a single Java file at a time with a single processor.
     *
     * <p>To add a new test for a rule with existing tests, add a new file to an existing test
     * directory in {@link ProcessorTestHelper#TEST_FILES_ROOT}. To add a new test for a rule
     * without existing tests, add a new directory+Java file in {@link
     * ProcessorTestHelper#TEST_FILES_ROOT} as described in the docs for {@link
     * ProcessorTestHelper#toProcessorTestCase(File)}.
     */
    @ParameterizedTest
    @ArgumentsSource(NonCompliantJavaFileProvider.class)
    public void testProcessSingleFile(
            ProcessorTestHelper.ProcessorTestCase<? extends JavaFileScanner> testCase)
            throws Exception {
        String pathToRepairedFile =
                Paths.get(Constants.SORALD_WORKSPACE)
                        .resolve(Constants.SPOONED)
                        .resolve(testCase.outfileRelpath)
                        .toString();
        String originalFileAbspath = testCase.nonCompliantFile.toPath().toAbsolutePath().toString();
        boolean brokenWithSniper = BROKEN_WITH_SNIPER.contains(testCase.checkClass);

        Verifier.verifyHasIssue(originalFileAbspath, testCase.createCheckInstance());
        Main.main(
                new String[] {
                    Constants.ARG_SYMBOL + Constants.ARG_ORIGINAL_FILES_PATH,
                    originalFileAbspath,
                    Constants.ARG_SYMBOL + Constants.ARG_RULE_KEYS,
                    testCase.ruleKey,
                    Constants.ARG_SYMBOL + Constants.ARG_WORKSPACE,
                    Constants.SORALD_WORKSPACE,
                    Constants.ARG_SYMBOL + Constants.ARG_PRETTY_PRINTING_STRATEGY,
                    brokenWithSniper
                            ? PrettyPrintingStrategy.NORMAL.name()
                            : PrettyPrintingStrategy.SNIPER.name()
                });

        TestHelper.removeComplianceComments(pathToRepairedFile);
        Verifier.verifyNoIssue(pathToRepairedFile, testCase.createCheckInstance());
    }

    /**
     * Provider class that provides test cases based on the buggy/non-compliant Java source files in
     * the test files directory.
     */
    private static class NonCompliantJavaFileProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return Arrays.stream(ProcessorTestHelper.TEST_FILES_ROOT.toFile().listFiles())
                    .filter(File::isDirectory)
                    .flatMap(
                            dir ->
                                    Arrays.stream(dir.listFiles())
                                            .filter(file -> file.getName().endsWith(".java"))
                                            .map(ProcessorTestHelper::toProcessorTestCase))
                    .map(Arguments::of);
        }
    }
}
