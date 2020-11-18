package sorald.miner;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.eclipse.jgit.api.Git;
import org.sonar.plugins.java.api.JavaFileScanner;
import sorald.Constants;
import sorald.sonar.RuleVerifier;
import sorald.sonar.RuleViolation;

public class MineSonarWarnings {

    public static void printUsage(JSAP jsap) {
        System.err.println("Arguments: ");
        System.err.println();
        System.err.println(jsap.getHelp());
        System.exit(-1);
    }

    public static void mineGitRepos(
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

    public static void mineLocalProject(
            List<? extends JavaFileScanner> checks, String projectPath) {
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
    static Map<String, Integer> extractWarnings(
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
        RuleVerifier.analyze(filesToScan, file, checks).stream()
                .map(RuleViolation::getCheckName)
                .forEach(incrementWarningCount);
        return warnings;
    }
}
