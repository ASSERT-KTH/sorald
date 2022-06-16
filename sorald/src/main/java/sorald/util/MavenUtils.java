package sorald.util;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import spoon.MavenLauncher;

/** Utility methods for working with Maven projects */
public class MavenUtils {
    private MavenUtils() {}

    /**
     * Resolve the full classpath for a Maven project.
     *
     * @param source Path to the root of a Maven project
     * @return The full source classpath
     */
    public static List<String> resolveClasspath(Path source) {
        MavenLauncher launcher =
                new MavenLauncher(source.toString(), MavenLauncher.SOURCE_TYPE.ALL_SOURCE);
        System.out.println("Spoon: " + Arrays.toString(launcher.getEnvironment().getSourceClasspath()));
        return List.of(launcher.getEnvironment().getSourceClasspath());
    }

    /**
     * Test whether or not the given source path points to the root of a Maven project.
     *
     * @param source A path
     * @return true iff the source path points to the root of a Maven project
     */
    public static boolean isMavenProjectRoot(Path source) {
        return source.resolve("pom.xml").toFile().isFile();
    }
}
