package sorald;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import sorald.processor.ProcessorTestHelper;

public class MavenHelper {
    static void convertToMavenProject(File projectDir) throws IOException {
        List<File> packageDirs = new ArrayList<>();

        for (File testDir : projectDir.listFiles()) {
            if (!testDir.isDirectory()) {
                continue;
            }

            String dirNameWithoutPrefix = testDir.getName().substring(5);
            File mavenTestDir = testDir.toPath().resolveSibling(dirNameWithoutPrefix).toFile();
            boolean renamed = testDir.renameTo(mavenTestDir);
            assertTrue(renamed);
            packageDirs.add(mavenTestDir);

            for (File javaFile : mavenTestDir.listFiles()) {
                if (!javaFile.getName().endsWith(Constants.JAVA_EXT)) {
                    continue;
                } else if (ProcessorTestHelper.isIgnoredTestFile(javaFile)
                        || !ProcessorTestHelper.isStandaloneCompilableTestFile(javaFile)) {
                    boolean deleted = javaFile.delete();
                    assertTrue(deleted);
                    continue;
                }

                String contentWithoutPackage =
                        Files.readAllLines(javaFile.toPath()).stream()
                                .filter(line -> !line.matches("package .*;"))
                                .collect(Collectors.joining("\n"));
                String packageStatement = String.format("package %s;\n", dirNameWithoutPrefix);
                Files.writeString(javaFile.toPath(), packageStatement + contentWithoutPackage);
            }
        }

        File srcDir = projectDir.toPath().resolve("src/main/java").toFile();
        boolean dirCreated = srcDir.mkdirs();
        assertTrue(dirCreated);

        packageDirs.forEach(
                pkgDir -> pkgDir.renameTo(srcDir.toPath().resolve(pkgDir.getName()).toFile()));

        File pomFile =
                Paths.get(Constants.PATH_TO_RESOURCES_FOLDER)
                        .resolve("scenario_test_files/simple-java8-maven-project/pom.xml")
                        .toFile();
        org.apache.commons.io.FileUtils.copyFile(
                pomFile, projectDir.toPath().resolve("pom.xml").toFile());
    }
}
