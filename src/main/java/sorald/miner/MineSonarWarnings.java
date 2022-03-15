package sorald.miner;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import org.eclipse.jgit.api.Git;
import sorald.FileUtils;
import sorald.cli.CommandConfiguration;
import sorald.event.EventHelper;
import sorald.event.EventType;
import sorald.event.SoraldEventHandler;
import sorald.event.models.miner.MinedViolationEvent;
import sorald.rule.Rule;
import sorald.rule.RuleViolation;
import sorald.sonar.ProjectScanner;

public class MineSonarWarnings {
    final List<SoraldEventHandler> eventHandlers;
    private final List<String> classpath;

    public MineSonarWarnings(
            List<? extends SoraldEventHandler> eventHandlers, List<String> classpath) {
        this.eventHandlers = Collections.unmodifiableList(eventHandlers);
        this.classpath = classpath;
    }

    public void mineGitRepos(
            String outputPath,
            List<String> reposList,
            File repoDir,
            CommandConfiguration soraldConfiguration)
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

            Map<String, Integer> warnings =
                    extractWarnings(repoDir.getAbsolutePath(), soraldConfiguration);

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

    public void mineLocalProject(String projectPath, CommandConfiguration soraldConfiguration) {
        Map<String, Integer> warnings = extractWarnings(projectPath, soraldConfiguration);

        warnings.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .forEach(System.out::println);
    }

    /**
     * @param projectPath The root path to a Java project
     * @return A mapping (checkClassName<ruleKey> -> numViolations)
     */
    Map<String, Integer> extractWarnings(
            String projectPath, CommandConfiguration soraldConfiguration) {
        final Map<Rule, Integer> warnings = new HashMap<>();
        final var target = new File(projectPath);

        EventHelper.fireEvent(EventType.MINING_START, eventHandlers);
        Set<RuleViolation> ruleViolations =
                ProjectScanner.scanProject(
                        target,
                        FileUtils.getClosestDirectory(target),
                        classpath,
                        soraldConfiguration);
        EventHelper.fireEvent(EventType.MINING_END, eventHandlers);

        ruleViolations.stream()
                .map(RuleViolation::getRuleKey)
                .map(Rule::of)
                .forEach(
                        rv -> {
                            if (warnings.containsKey(rv)) {
                                warnings.put(rv, warnings.get(rv) + 1);
                            } else {
                                warnings.put(rv, 1);
                            }
                        });

        ruleViolations.forEach(
                v ->
                        EventHelper.fireEvent(
                                new MinedViolationEvent(v, Paths.get(projectPath)), eventHandlers));

        Map<String, Integer> warningsWithUpdateKeys = new HashMap<>();
        warnings.forEach((rule, count) -> warningsWithUpdateKeys.put(rule.getKey(), count));

        return warningsWithUpdateKeys;
    }
}
