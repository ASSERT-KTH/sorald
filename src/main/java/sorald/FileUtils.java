package sorald;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {

    /**
     * Compare the two given paths as absolute, normalized paths.
     *
     * @param lhs A path.
     * @param rhs A path.
     * @return Whether or not the paths are equal as absolute, normalized paths.
     */
    public static boolean pathAbsNormEqual(String lhs, String rhs) {
        return pathAbsNormEqual(Paths.get(lhs), Paths.get(rhs));
    }

    /**
     * Compare the two given paths as absolute, normalized paths.
     *
     * @param lhs A path.
     * @param rhs A path.
     * @return Whether or not the paths are equal as absolute, normalized paths.
     */
    public static boolean pathAbsNormEqual(Path lhs, Path rhs) {
        return lhs.toAbsolutePath().normalize().equals(rhs.toAbsolutePath().normalize());
    }

    /**
     * @param file A file.
     * @return The given file if it is a directory, or its parent directory if it is not a
     *     directory.
     */
    public static File getClosestDirectory(File file) {
        return file.isDirectory() ? file : file.getParentFile();
    }
}
