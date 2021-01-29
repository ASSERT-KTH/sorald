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

public class MavenHelper {
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
            Path packageAbsPath = productionSourceDir.resolve(packageRelPath);

            if (!packageAbsPath.toFile().isDirectory()) {
                boolean created = packageAbsPath.toFile().mkdirs();
                assertTrue(created);
            }

            boolean moved = javaFile.renameTo(packageAbsPath.resolve(javaFile.getName()).toFile());
            assertTrue(moved);
        }

        File pomFile =
                Paths.get(Constants.PATH_TO_RESOURCES_FOLDER)
                        .resolve("scenario_test_files/maven_converter_pom.xml")
                        .toFile();
        org.apache.commons.io.FileUtils.copyFile(
                pomFile, projectDir.toPath().resolve("pom.xml").toFile());
    }
}
