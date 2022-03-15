package sorald.rule;

import java.io.File;
import java.util.Collection;
import java.util.List;
import sorald.cli.CommandConfiguration;

/** A static analyzer for Java source code */
public interface StaticAnalyzer {

    /**
     * Scan files for violations of some rules.
     *
     * @param files The files to analyze.
     * @param rule The rules to use.
     * @param classpath Classpath that includes any dependencies.
     * @return All violations of the rules found in the files.
     */
    Collection<RuleViolation> findViolations(
            List<File> files, List<String> classpath, CommandConfiguration soraldConfiguration);
}
