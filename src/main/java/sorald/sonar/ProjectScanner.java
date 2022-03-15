package sorald.sonar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import sorald.Constants;
import sorald.FileUtils;
import sorald.cli.CommandConfiguration;
import sorald.rule.Rule;
import sorald.rule.RuleViolation;

/** Helper class that uses Sonar to scan projects for rule violations. */
public class ProjectScanner {
    private ProjectScanner() {}

    /**
     * Scan a project for rule violations.
     *
     * @param target Targeted file or directory of the project.
     * @param baseDir Base directory of the project.
     * @param rule Rule to scan for.
     * @return All violations in the target.
     */
    public static Set<RuleViolation> scanProject(File target, File baseDir, Rule rule) {
        return scanProject(target, baseDir, List.of(rule));
    }

    /**
     * Scan a project for rule violations.
     *
     * @param target Targeted file or directory of the project.
     * @param baseDir Base directory of the project.
     * @param rules Rules to scan for.
     * @return All violations in the target.
     */
    public static Set<RuleViolation> scanProject(File target, File baseDir, List<Rule> rules) {
        CommandConfiguration soraldConfiguration = new CommandConfiguration(rules);
        return scanProject(target, baseDir, List.of(), soraldConfiguration);
    }

    /**
     * Scan a project for rule violations, with additional type information collected from the
     * provided classpath.
     *
     * @param target Targeted file or directory of the project.
     * @param baseDir Base directory of the project.
     * @param classpath Classpath to fetch type information from.
     * @param soraldConfiguration configuration provided via CLI.
     * @return All violations in the target.
     */
    public static Set<RuleViolation> scanProject(
            File target,
            File baseDir,
            List<String> classpath,
            CommandConfiguration soraldConfiguration) {
        List<File> filesToScan = new ArrayList<>();
        if (target.isFile()) {
            filesToScan.add(target);
        } else {
            try {
                filesToScan = FileUtils.findFilesByExtension(target, Constants.JAVA_EXT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // TODO generalize to not directly use the SonarStaticAnalyzer
        var violations =
                new SonarStaticAnalyzer(baseDir)
                        .findViolations(filesToScan, classpath, soraldConfiguration);
        return new HashSet<>(violations);
    }
}
