package sorald;

import sorald.annotations.ProcessorsClassGenerator;
import spoon.Launcher;

/** Wrapper class for all (present and future) code generation in Sorald. */
public class CodeGenerator {

    public static void main(String[] args) {
        generateSources();
    }

    private static void generateSources() {
        final Launcher launcher = new Launcher();
        launcher.getEnvironment().setAutoImports(true);
        launcher.getEnvironment().setNoClasspath(true);
        launcher.getEnvironment().setCommentEnabled(true);

        launcher.setSourceOutputDirectory("./src/main/java");
        launcher.addInputResource("src/main/java/sorald");

        launcher.addProcessor(new ProcessorsClassGenerator<>());
        launcher.setOutputFilter("sorald.Processors");

        launcher.run();
    }
}
