package sorald;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.json.JSONObject;
import sorald.event.StatisticsCollector;
import sorald.event.StatsMetadataKeys;

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

    /**
     * Delete a directory.
     *
     * @param directoryToBeDeleted The directory to delete
     * @return true if the directory was successfully deleted
     */
    public static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    /**
     * Search for files with the given file extension in the given directory, as well as recursively
     * in subdirectories.
     *
     * @param directory A directory
     * @param ext A file extension including the leading dot
     * @return All files in the given directory or any subdirectory with a matching extension
     */
    public static List<File> findFilesByExtension(File directory, String ext) throws IOException {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException(directory.toString() + " is not a directory");
        }
        try (Stream<Path> files = Files.walk(directory.toPath())) {
            return files.map(Path::toFile)
                    .filter(File::isFile)
                    .filter(f -> getExtension(f).equals(ext))
                    .collect(Collectors.toList());
        }
    }

    /**
     * @param file A file
     * @return The file extension of the given file including the dot, or the empty string if it has
     *     no extension
     */
    public static String getExtension(File file) {
        String[] parts = file.getName().split("\\.");
        return parts.length <= 1 ? "" : "." + parts[parts.length - 1];
    }

    /**
     * Write the statistics output file.
     *
     * @param statsOutputFile The file to write to.
     * @param statsCollector A {@link StatisticsCollector} containing stats.
     * @param originalArgs The original arguments passed to the command line.
     * @throws IOException If the file can't be written to.
     */
    public static void writeStatistics(
            File statsOutputFile, StatisticsCollector statsCollector, List<String> originalArgs)
            throws IOException {
        JSONObject jo = new JSONObject(statsCollector);
        jo.put(StatsMetadataKeys.ORIGINAL_ARGS, originalArgs);
        Files.writeString(
                statsOutputFile.toPath(),
                jo.toString(4),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * @param jsonFile Path to a JSON file to read.
     * @return A parsed JSON object.
     * @throws IOException If the file can't be read.
     */
    public static JSONObject readJSON(Path jsonFile) throws IOException {
        String content = Files.readString(jsonFile);
        return new JSONObject(content);
    }
}
