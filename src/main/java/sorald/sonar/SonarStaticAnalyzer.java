package sorald.sonar;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import sorald.rule.Rule;
import sorald.rule.RuleViolation;
import sorald.rule.StaticAnalyzer;

public class SonarStaticAnalyzer implements StaticAnalyzer {
    private final File projectRoot;

    public SonarStaticAnalyzer(File projectRoot) {
        this.projectRoot = projectRoot;
    }

    @Override
    public Collection<RuleViolation> findViolations(
            List<File> files, List<Rule> rules, List<String> classpath) {
        var checks =
                rules.stream()
                        .map(Rule::getKey)
                        .map(Checks::getCheckInstance)
                        .collect(Collectors.toList());
        var filepaths = files.stream().map(File::toString).collect(Collectors.toList());
        return RuleVerifier.analyze(filepaths, projectRoot, checks, classpath);
    }
}
