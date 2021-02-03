package sorald;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import sorald.processor.ProcessorTestHelper;

/** A class for helping with Maven in testing. */
public class MavenHelper {
    private static final File POM_FILE =
            Paths.get(Constants.PATH_TO_RESOURCES_FOLDER)
                    .resolve("scenario_test_files/maven_converter_pom.xml")
                    .toFile();

    /**
     * Convert a directory with Java source code into a Maven project. Source code in nested
     * directories are placed into packages with package names corresponding to the directory paths.
     *
     * <p>Does not include source files prefixed with NOCOMPILE or IGNORE.
     *
     * <p>Note that this method MUTATES the provided project directory to become a Maven project.
     *
     * @param projectDir The root directory to convert into a Maven project.
     * @throws IOException
     */
    static void convertToMavenProject(File projectDir) throws IOException {
        List<File> javaFiles = FileUtils.findFilesByExtension(projectDir, Constants.JAVA_EXT);

        Path productionSourceDir = projectDir.toPath().resolve("src/main/java");

        for (File javaFile : javaFiles) {
            if (ProcessorTestHelper.isIgnoredTestFile(javaFile)
                    || !ProcessorTestHelper.isStandaloneCompilableTestFile(javaFile)) {
                boolean deleted = javaFile.delete();
                assertTrue(deleted);
                continue;
            }

            Path packagePath = projectDir.toPath().relativize(javaFile.getParentFile().toPath());
            packageJavaFile(javaFile, productionSourceDir, packagePath);
        }

        org.apache.commons.io.FileUtils.copyFile(
                POM_FILE, projectDir.toPath().resolve("pom.xml").toFile());
    }

    /**
     * Take a Java file, move it into the package indicated by packagePath with prodSrcDir as the
     * root Java source directory. Also change the package statement in the file to match the
     * provided package path.
     */
    private static void packageJavaFile(File javaFile, Path prodSrcDir, Path packagePath)
            throws IOException {
        List<String> sanitizedPathComponents =
                StreamSupport.stream(packagePath.spliterator(), false)
                        .map(path -> path.toString().replaceAll("[^a-zA-Z]", ""))
                        .collect(Collectors.toList());

        String packageName = String.join(".", sanitizedPathComponents);
        String contentWithoutPackage =
                Files.readAllLines(javaFile.toPath()).stream()
                        .filter(line -> !line.matches("package .*;"))
                        .collect(Collectors.joining("\n"));
        String packageStatement =
                packageName.isEmpty() ? "" : String.format("package %s;\n", packageName);
        Files.writeString(javaFile.toPath(), packageStatement + contentWithoutPackage);

        Path packageRelPath = Paths.get(String.join(File.separator, sanitizedPathComponents));
        Path packageAbsPath = prodSrcDir.resolve(packageRelPath);

        if (!packageAbsPath.toFile().isDirectory()) {
            boolean created = packageAbsPath.toFile().mkdirs();
            assertTrue(created);
        }

        boolean moved = javaFile.renameTo(packageAbsPath.resolve(javaFile.getName()).toFile());
        assertTrue(moved);
    }
}
