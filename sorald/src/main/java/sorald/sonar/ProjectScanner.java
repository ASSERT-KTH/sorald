package sorald.sonar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import sorald.Constants;
import sorald.FileUtils;
import sorald.cli.CLIConfigForStaticAnalyzer;
import sorald.rule.Rule;
import sorald.rule.RuleViolation;
import sorald.rule.StaticAnalyzer;

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
        return scanProject(target, baseDir, List.of(rule), null);
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
        return scanProject(target, baseDir, rules, null);
    }

    /**
     * Scan a project for rule violations, with additional type information collected from the
     * provided classpath.
     *
     * @param target Targeted file or directory of the project.
     * @param baseDir Base directory of the project.
     * @param rules Rules to scan for.
     * @param cliOptions Options for static analyzer.
     * @return All violations in the target.
     */
    public static Set<RuleViolation> scanProject(
            File target, File baseDir, List<Rule> rules, CLIConfigForStaticAnalyzer cliOptions) {
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
        ServiceLoader<StaticAnalyzer> analyzers = ServiceLoader.load(StaticAnalyzer.class);
        Set<RuleViolation> violations = new HashSet<>();
        for (StaticAnalyzer analyzer : analyzers) {
            violations.addAll(analyzer.findViolations(baseDir, filesToScan, rules, cliOptions));
        }
        return new HashSet<>(violations);
    }
}
