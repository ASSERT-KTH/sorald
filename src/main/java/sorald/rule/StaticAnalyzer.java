package sorald.rule;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * A static analyzer for Java source code.
 *
 * @param <T> rule type according to the static analyzer
 */
public interface StaticAnalyzer<T extends Rule<? extends RuleType>> {

    /**
     * Scan files for violations of some rules.
     *
     * @param files The files to analyze.
     * @param rule The rules to use.
     * @param classpath Classpath that includes any dependencies.
     * @return All violations of the rules found in the files.
     */
    Collection<RuleViolation> findViolations(
            List<File> files, List<T> rule, List<String> classpath);
}
