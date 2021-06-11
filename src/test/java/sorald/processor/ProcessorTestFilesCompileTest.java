package sorald.processor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Meta tests for verifying that the processor test files compile as expected. */
class ProcessorTestFilesCompileTest {

    @ParameterizedTest
    @MethodSource("provideCompilableProcessorTestInputFile")
    void processorTestCaseInputFile_notMarkedNOCOMPILE_shouldCompile(File testCaseJavaFile)
            throws IOException {
        assertCompiles(testCaseJavaFile);
    }

    private static Stream<Arguments> provideCompilableProcessorTestInputFile() {
        return getCompilableProcessorTestCases().map(tc -> tc.nonCompliantFile).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("provideCompilableProcessorTestExpectedFiles")
    void processorTestCaseExpectedFile_notMarkedNOCOMPILE_shouldCompile(
            File testCaseExpectedJavaFile, @TempDir File tempDir) throws IOException {
        File javaFile = copyFileToDirWithoutExpectedExtension(testCaseExpectedJavaFile, tempDir);
        assertCompiles(javaFile);
    }

    private static Stream<Arguments> provideCompilableProcessorTestExpectedFiles() {
        return getCompilableProcessorTestCases()
                .flatMap(tc -> tc.expectedOutfile().stream())
                .map(Arguments::of);
    }

    private static File copyFileToDirWithoutExpectedExtension(File expectedJavaFile, File dir)
            throws IOException {
        String fileNameWithExpectedExtension = expectedJavaFile.getName();
        String validJavaFileName =
                fileNameWithExpectedExtension.substring(
                        0, fileNameWithExpectedExtension.lastIndexOf("."));
        File validJavaFile = dir.toPath().resolve(validJavaFileName).toFile();
        FileUtils.copyFile(expectedJavaFile, validJavaFile);
        return validJavaFile;
    }

    private static Stream<ProcessorTestHelper.ProcessorTestCase<?>>
            getCompilableProcessorTestCases() {
        return ProcessorTestHelper.getTestCaseStream(ProcessorTestHelper.TEST_FILES_ROOT.toFile())
                .filter(
                        tc ->
                                ProcessorTestHelper.isStandaloneCompilableTestFile(
                                        tc.nonCompliantFile));
    }

    private static void assertCompiles(File javaFile) throws IOException {
        var compiler = ToolProvider.getSystemJavaCompiler();
        assertThat(
                "System does not have a Java compiler, please run test suite with a JDK",
                compiler,
                notNullValue());

        var diagnostics = new DiagnosticCollector<JavaFileObject>();
        var fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        var compilationUnits =
                fileManager.getJavaFileObjectsFromStrings(List.of(javaFile.getAbsolutePath()));
        var task = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);

        boolean success = task.call();

        List<String> messages =
                diagnostics.getDiagnostics().stream()
                        .map(Diagnostic::toString)
                        .collect(Collectors.toList());

        fileManager.close();

        assertTrue(success, String.join(System.lineSeparator(), messages));
    }
}
