package sorald.miner;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.eclipse.jgit.api.Git;
import sorald.FileUtils;
import sorald.cli.CLIConfigForStaticAnalyzer;
import sorald.event.EventHelper;
import sorald.event.EventType;
import sorald.event.SoraldEventHandler;
import sorald.event.models.miner.MinedViolationEvent;
import sorald.rule.Rule;
import sorald.rule.RuleViolation;
import sorald.sonar.ProjectScanner;
import sorald.sonar.SonarRule;

public class MineSonarWarnings {
    final List<SoraldEventHandler> eventHandlers;
    private final CLIConfigForStaticAnalyzer cliOptions;

    public MineSonarWarnings(
            List<? extends SoraldEventHandler> eventHandlers,
            CLIConfigForStaticAnalyzer cliOptions) {
        this.eventHandlers = Collections.unmodifiableList(eventHandlers);
        this.cliOptions = cliOptions;
    }

    public void mineGitRepos(
            List<Rule> rules, String outputPath, List<String> reposList, File repoDir)
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

            Map<String, Integer> warnings = extractWarnings(repoDir.getAbsolutePath(), rules);

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

    public void mineLocalProject(List<Rule> rules, String projectPath) {
        Map<String, Integer> warnings = extractWarnings(projectPath, rules);

        warnings.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .forEach(entry -> new SystemStreamLog().info(entry.toString()));
    }

    /**
     * @param projectPath The root path to a Java project
     * @param rules Rules to find violations of in the Java files in the project
     * @return A mapping (checkClassName<ruleKey> -> numViolations)
     */
    Map<String, Integer> extractWarnings(String projectPath, List<Rule> rules) {
        final Map<Rule, Integer> warnings = new HashMap<>();
        final var target = new File(projectPath);

        rules.forEach(ruleName -> warnings.put(ruleName, 0));

        Consumer<Rule> incrementWarningCount = (rule) -> warnings.put(rule, warnings.get(rule) + 1);

        EventHelper.fireEvent(EventType.MINING_START, eventHandlers);
        Set<RuleViolation> ruleViolations =
                ProjectScanner.scanProject(
                        target, FileUtils.getClosestDirectory(target), rules, cliOptions);
        EventHelper.fireEvent(EventType.MINING_END, eventHandlers);

        ruleViolations.stream()
                .map(RuleViolation::getRuleKey)
                .map(SonarRule::new)
                .forEach(incrementWarningCount);

        ruleViolations.forEach(
                v ->
                        EventHelper.fireEvent(
                                new MinedViolationEvent(v, Paths.get(projectPath)), eventHandlers));

        Map<String, Integer> warningsWithUpdateKeys = new HashMap<>();
        warnings.forEach((rule, count) -> warningsWithUpdateKeys.put(rule.getKey(), count));

        return warningsWithUpdateKeys;
    }
}
