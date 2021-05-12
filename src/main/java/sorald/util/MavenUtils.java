package sorald.util;

import java.nio.file.Path;
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
     * @throws IllegalArgumentException If source does not point to the root of a Maven project
     */
    public static List<String> resolveClasspath(Path source) {
        if (!isMavenProjectRoot(source)) {
            throw new IllegalArgumentException(
                    String.format("'%s' is not a Maven project root", source));
        }

        MavenLauncher launcher =
                new MavenLauncher(source.toString(), MavenLauncher.SOURCE_TYPE.ALL_SOURCE);
        return List.of(launcher.getEnvironment().getSourceClasspath());
    }

    private static boolean isMavenProjectRoot(Path source) {
        return source.resolve("pom.xml").toFile().isFile();
    }
}
