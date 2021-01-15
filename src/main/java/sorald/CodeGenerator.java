package sorald;

import java.nio.file.Path;
import java.nio.file.Paths;
import sorald.annotations.ProcessorsClassGenerator;
import spoon.Launcher;

/** Wrapper class for all (present and future) code generation in Sorald. */
public class CodeGenerator {

    public static void main(String[] args) {
        generateSources(Paths.get("src/main/java"));
    }

    static void generateSources(Path outputDirectory) {
        final Launcher launcher = new Launcher();
        launcher.getEnvironment().setAutoImports(true);
        launcher.getEnvironment().setNoClasspath(true);
        launcher.getEnvironment().setCommentEnabled(true);

        launcher.setSourceOutputDirectory(outputDirectory.toString());
        launcher.addInputResource("src/main/java/sorald");

        launcher.addProcessor(new ProcessorsClassGenerator<>());
        launcher.setOutputFilter("sorald.Processors");

        launcher.run();
    }
}
