package sorald.sonar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.sonar.plugins.java.api.JavaFileScanner;
import sorald.Constants;
import sorald.FileUtils;
import sorald.segment.Node;

/** Helper class that uses Sonar to scan projects for rule violations. */
public class ProjectScanner {

    /**
     * Scan a project for rule violations.
     *
     * @param target Targeted file or directory of the project.
     * @param baseDir Base directory of the project.
     * @param sonarCheck The check to scan with.
     * @return All violations in the target.
     */
    public static Set<RuleViolation> scanProject(
            File target, File baseDir, JavaFileScanner sonarCheck) {
        List<String> filesToScan = new ArrayList<>();
        if (target.isFile()) {
            filesToScan.add(target.getAbsolutePath());
        } else {
            try {
                filesToScan =
                        FileUtils.findFilesByExtension(target, Constants.JAVA_EXT).stream()
                                .map(File::getAbsolutePath)
                                .collect(Collectors.toList());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return RuleVerifier.analyze(filesToScan, baseDir, sonarCheck);
    }

    /**
     * Scan a segment of a project for rule violations.
     *
     * @param segment A segment.
     * @param baseDir Base directory of the project.
     * @param sonarCheck The check to scan with.
     * @return All violations in the segments.
     */
    public static Set<RuleViolation> scanSegment(
            List<Node> segment, File baseDir, JavaFileScanner sonarCheck) {
        List<String> filesToScan = new ArrayList<>();
        for (Node node : segment) {
            if (node.isFileNode()) {
                filesToScan.addAll(node.getJavaFiles());
            } else {
                try (Stream<Path> walk = Files.walk(Paths.get(node.getRootPath()))) {
                    filesToScan.addAll(
                            walk.map(x -> x.toFile().getAbsolutePath())
                                    .filter(f -> f.endsWith(Constants.JAVA_EXT))
                                    .collect(Collectors.toList()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return RuleVerifier.analyze(filesToScan, baseDir, sonarCheck);
    }
}
