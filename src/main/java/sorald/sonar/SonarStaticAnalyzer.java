package sorald.sonar;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.sonarsource.sonarlint.core.client.api.common.RuleKey;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;
import org.sonarsource.sonarlint.core.client.api.common.analysis.IssueListener;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneAnalysisConfiguration;
import sorald.rule.Rule;
import sorald.rule.RuleViolation;
import sorald.rule.StaticAnalyzer;

public class SonarStaticAnalyzer implements StaticAnalyzer {
    private final File projectRoot;
    private static SonarLintEngine sonarLint;

    public SonarStaticAnalyzer(File projectRoot) {
        this.projectRoot = projectRoot;
        sonarLint = SonarLintEngine.getInstance();
    }

    @Override
    public Collection<RuleViolation> findViolations(
            List<File> files, List<Rule> rules, List<String> classpath) {
        return analyze(files, rules, classpath);
    }

    private Collection<RuleViolation> analyze(
            List<File> files, List<Rule> rules, List<String> classpath) {

        List<JavaInputFile> inputFiles =
                files.stream()
                        .map(File::toPath)
                        .map(JavaInputFile::new)
                        .collect(Collectors.toList());
        StandaloneAnalysisConfiguration config =
                StandaloneAnalysisConfiguration.builder()
                        .setBaseDir(projectRoot.toPath())
                        // SonarLint takes classpath as a comma separated string to make it OS
                        // independent.
                        // See:
                        // https://github.com/SonarSource/sonar-java/blob/6050868761069bc5ff965a149f2fd9a64d6319e0/sonar-java-plugin/src/main/resources/static/documentation.md#java-analysis-and-bytecode
                        .putExtraProperty("sonar.java.libraries", String.join(",", classpath))
                        .addIncludedRules(
                                rules.stream()
                                        .map(rule -> RuleKey.parse("java:" + rule.getKey()))
                                        .collect(Collectors.toList()))
                        .addInputFiles(inputFiles)
                        .build();

        var issueHandler = new IssueHandler();
        sonarLint.analyze(config, issueHandler, null, null);
        sonarLint.stop();
        return issueHandler.issues.stream().map(ScannedViolation::new).collect(Collectors.toList());
    }

    private static class IssueHandler implements IssueListener {
        private final List<Issue> issues = new ArrayList<>();

        @Override
        public void handle(@Nonnull Issue issue) {
            issues.add(issue);
        }
    }
}
