package sorald;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.PrettyPrinter;

public class CodeGeneratorTest {

    /** Test that there is no difference between the generated code and the committed code. */
    @Test
    public void generateSources_generatesCommittedProcessorsClass(
            @TempDir File generatedSourceDir) {
        Path sourceDir = Paths.get("src/main/java");

        CodeGenerator.generateSources(generatedSourceDir.toPath());

        CtType<?> committed = parseJavaFile(sourceDir.resolve("sorald/Processors.java"));
        CtType<?> generated = parseJavaFile(generatedSourceDir.toPath().resolve("sorald/"));

        assertThat(printType(generated), equalTo(printType(committed)));
    }

    private static CtType<?> parseJavaFile(Path javaFile) {
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setCommentEnabled(true);
        launcher.addInputResource(javaFile.toString());

        CtModel model = launcher.buildModel();
        return model.getAllTypes().stream().filter(CtType::isTopLevel).findFirst().get();
    }

    private static String printType(CtType<?> type) {
        PrettyPrinter printer = type.getFactory().getEnvironment().createPrettyPrinter();
        return printer.printTypes(type);
    }
}
