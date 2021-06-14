package sorald;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import org.apache.commons.lang3.tuple.Pair;

/** High-level assertions. */
public class Assertions {

    /**
     * Assert that the given Java file compiles on its own.
     *
     * @param javaFile A Java source file.
     */
    public static void assertCompiles(File javaFile) {
        var compileResults = compile(javaFile);
        boolean compileSuccessful = compileResults.getLeft();
        String diagnosticsOutput = compileResults.getRight();

        assertTrue(compileSuccessful, diagnosticsOutput);
    }

    /** Returns a pair (compileSuccess, diagnosticsOutput) */
    private static Pair<Boolean, String> compile(File javaFile) {
        // inspired comment by user GETah on StackOverflow: https://stackoverflow.com/a/8364016

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

        String diagnosticsOutput =
                diagnostics.getDiagnostics().stream()
                        .map(Diagnostic::toString)
                        .collect(Collectors.joining(System.lineSeparator()));

        return Pair.of(success, diagnosticsOutput);
    }
}
