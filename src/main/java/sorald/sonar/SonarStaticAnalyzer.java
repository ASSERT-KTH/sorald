package sorald.sonar;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.sonarsource.sonarlint.core.client.api.common.RuleKey;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;
import org.sonarsource.sonarlint.core.client.api.common.analysis.IssueListener;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneAnalysisConfiguration;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneGlobalConfiguration;
import org.sonarsource.sonarlint.core.commons.Language;
import sorald.rule.Rule;
import sorald.rule.RuleViolation;
import sorald.rule.StaticAnalyzer;

public class SonarStaticAnalyzer implements StaticAnalyzer {
    private final File projectRoot;
    private static SonarLintEngine sonarLint;
    private static final Path sonarJavaPath =
            Paths.get("target/classes").resolve("sonar-java-plugin-6.12.0.24852.jar");

    public SonarStaticAnalyzer(File projectRoot) {
        this.projectRoot = projectRoot;

        if (sonarLint == null) {
            StandaloneGlobalConfiguration globalConfig =
                    StandaloneGlobalConfiguration.builder()
                            .addPlugin(sonarJavaPath)
                            .addEnabledLanguage(Language.JAVA)
                            .build();
            sonarLint = new SonarLintEngine(globalConfig);
        } else {
            sonarLint.recreateAnalysisEngine();
        }
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
