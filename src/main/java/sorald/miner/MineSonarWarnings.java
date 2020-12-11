package sorald.miner;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.eclipse.jgit.api.Git;
import org.sonar.plugins.java.api.JavaFileScanner;
import sorald.Constants;
import sorald.event.EventHelper;
import sorald.event.EventType;
import sorald.event.SoraldEventHandler;
import sorald.event.models.miner.MinedViolationEvent;
import sorald.sonar.RuleVerifier;
import sorald.sonar.RuleViolation;

public class MineSonarWarnings {
    final List<SoraldEventHandler> eventHandlers;

    public MineSonarWarnings(List<? extends SoraldEventHandler> eventHandlers) {
        this.eventHandlers = Collections.unmodifiableList(eventHandlers);
    }

    public void mineGitRepos(
            List<? extends JavaFileScanner> checks,
            String outputPath,
            List<String> reposList,
            File repoDir)
            throws IOException {
        // stats on a list of git repos
        for (String repo : reposList) {
            String repoName = repo.substring(repo.lastIndexOf('/') + 1, repo.lastIndexOf("."));

            org.apache.commons.io.FileUtils.cleanDirectory(repoDir);

            boolean isCloned = false;

            try {
                Git git = Git.cloneRepository().setURI(repo).setDirectory(repoDir).call();
                git.close();
                isCloned = true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            Map<String, Integer> warnings = extractWarnings(repoDir.getAbsolutePath(), checks);

            PrintWriter pw = new PrintWriter(new FileWriter(outputPath, true));

            if (isCloned) {
                pw.println("RepoName: " + repoName);

                warnings.entrySet().stream()
                        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                        .forEach(pw::println);
            } else {
                pw.println("RepoName: " + repoName + " not_cloned");
            }

            pw.flush();
            pw.close();
        }
    }

    public void mineLocalProject(List<? extends JavaFileScanner> checks, String projectPath) {
        Map<String, Integer> warnings = extractWarnings(projectPath, checks);

        warnings.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .forEach(System.out::println);
    }

    /**
     * @param projectPath The root path to a Java project
     * @param checks Checks to run on the Java files in the project
     * @return A mapping (checkClassName -> numViolations)
     */
    Map<String, Integer> extractWarnings(
            String projectPath, List<? extends JavaFileScanner> checks) {
        List<String> filesToScan = new ArrayList<>();
        File file = new File(projectPath);
        if (file.isFile()) {
            filesToScan.add(file.getAbsolutePath());
        } else {
            try {
                filesToScan =
                        sorald.FileUtils.findFilesByExtension(file, Constants.JAVA_EXT).stream()
                                .map(File::toString)
                                .collect(Collectors.toList());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        final Map<String, Integer> warnings = new HashMap<>();
        checks.stream()
                .map(Object::getClass)
                .map(Class::getSimpleName)
                .forEach(checkName -> warnings.put(checkName, 0));

        Consumer<String> incrementWarningCount =
                (checkName) -> warnings.put(checkName, warnings.get(checkName) + 1);

        EventHelper.fireEvent(EventType.MINING_START, eventHandlers);
        Set<RuleViolation> analyzeMessages = RuleVerifier.analyze(filesToScan, file, checks);
        EventHelper.fireEvent(EventType.MINING_END, eventHandlers);

        analyzeMessages.stream().map(RuleViolation::getCheckName).forEach(incrementWarningCount);

        analyzeMessages.forEach(
                v ->
                        EventHelper.fireEvent(
                                new MinedViolationEvent(v, Paths.get(projectPath)), eventHandlers));

        return warnings;
    }
}
