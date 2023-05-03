package sorald;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.io.FileMatchers.anExistingDirectory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

public class FileUtilsTest {

    @Test
    public void findFilesByExtension_onlyReturnsFiles_whenDirectoriesHaveMatchingExtensions(
            @TempDir File workdir) throws Exception {
        // arrange
        org.apache.commons.io.FileUtils.copyDirectory(
                TestHelper.PATH_TO_RESOURCES_FOLDER.toFile(), workdir);
        Path javaExtDirpath = workdir.toPath().resolve("randomdir.java");
        Path javaFileInJavaExtDirpath = javaExtDirpath.resolve("SomeClass.java");
        Files.createDirectory(workdir.toPath().resolve("randomdir.java"));
        Files.createFile(javaFileInJavaExtDirpath);

        // act
        List<File> files = FileUtils.findFilesByExtension(workdir, Constants.JAVA_EXT);

        // assert
        assertTrue(files.stream().anyMatch(f -> f.toPath().equals(javaFileInJavaExtDirpath)));
        assertFalse(files.stream().anyMatch(f -> f.toPath().equals(javaExtDirpath)));
        assertFalse(files.stream().anyMatch(f -> !f.isFile()));
    }

    @Test
    public void findFilesByExtension_onlyReturnsFilesWithMatchingExtension() throws IOException {
        // act
        List<File> files =
                FileUtils.findFilesByExtension(
                        TestHelper.PATH_TO_RESOURCES_FOLDER.toFile(), Constants.JAVA_EXT);

        // assert
        Predicate<File> isJavaFile =
                f -> f.isFile() && FileUtils.getExtension(f).equals(Constants.JAVA_EXT);
        assertTrue(files.stream().allMatch(isJavaFile));
    }

    @Test
    public void findFilesByExtension_throws_whenDirectoryIsNotADirectory() {
        File notADirectory = new File("definitely/not/a/directory.nope");
        assertThrows(
                IllegalArgumentException.class,
                () -> FileUtils.findFilesByExtension(notADirectory, ""),
                notADirectory.toString() + " is not a directory");
    }

    @Test
    public void getExtension_returnsEmptyString_whenFileLacksExtension() {
        File fileWithoutExtension = new File("path/to/some/file");
        assertEquals("", FileUtils.getExtension(fileWithoutExtension));
    }

    /**
     * This is an unintuitive part of the method: if two paths are precisely equal, but they don't
     * exist in the file system, the method returns false!
     */
    @Test
    void realPathEquals_returnsFalse_whenEqualPathsDoNotExist() {
        Path lhs = Path.of("/a/path/that/certainly/does/not/exist");
        Path rhs = Path.of(lhs.toString());

        assertThat(FileUtils.realPathEquals(lhs, rhs), equalTo(false));
    }

    @Test
    void getCacheDir_shouldReturnAValidDirectoryOnAllSupportedOs() {
        File cacheDir = FileUtils.getCacheDir();

        assertThat(cacheDir, anExistingDirectory());
    }
}
