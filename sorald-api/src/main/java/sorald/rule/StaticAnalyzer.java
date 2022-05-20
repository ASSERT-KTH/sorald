package sorald.rule;

import java.io.File;
import java.util.Collection;
import java.util.List;
import sorald.cli.CLIConfigForStaticAnalyzer;

/** A static analyzer for Java source code */
public interface StaticAnalyzer {

    /**
     * Scan files for violations of some rules.
     *
     * @param projectRoot the root folder of the project.
     * @param files The files to analyze.
     * @param rule The rules to use.
     * @param cliOptions Options for the static analyzer.
     * @return All violations of the rules found in the files.
     */
    Collection<RuleViolation> findViolations(
            File projectRoot, List<File> files, List<Rule> rule, CLIConfigForStaticAnalyzer cliOptions);
}
