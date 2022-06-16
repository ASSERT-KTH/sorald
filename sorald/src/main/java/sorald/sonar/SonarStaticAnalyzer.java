package sorald.sonar;

import com.google.auto.service.AutoService;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.sonarsource.sonarlint.core.client.api.common.RuleKey;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;
import org.sonarsource.sonarlint.core.client.api.common.analysis.IssueListener;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneAnalysisConfiguration;
import sorald.SoraldConfig;
import sorald.cli.CLIConfigForStaticAnalyzer;
import sorald.rule.Rule;
import sorald.rule.RuleViolation;
import sorald.rule.StaticAnalyzer;

@AutoService(StaticAnalyzer.class)
public class SonarStaticAnalyzer implements StaticAnalyzer {

    @Override
    public Collection<RuleViolation> findViolations(
            File projectRoot,
            List<File> files,
            List<Rule> rules,
            CLIConfigForStaticAnalyzer cliOptions) {
        return analyze(projectRoot, files, rules, cliOptions);
    }

    private Collection<RuleViolation> analyze(
            File projectRoot,
            List<File> files,
            List<Rule> rules,
            CLIConfigForStaticAnalyzer cliOptions) {

        List<JavaInputFile> inputFiles =
                files.stream()
                        .map(File::toPath)
                        .map(JavaInputFile::new)
                        .collect(Collectors.toList());
        StandaloneAnalysisConfiguration config;
        if (cliOptions == null) {
            config = getAnalysisConfigurationWithoutCliOptions(projectRoot, inputFiles, rules);
        } else {
            config =
                    getAnalysisConfigurationWithCliOptions(
                            projectRoot, inputFiles, rules, cliOptions);
        }

        SonarLintEngine sonarLint = SonarLintEngine.getInstance();
        var issueHandler = new IssueHandler();
        sonarLint.analyze(config, issueHandler, null, null);
        sonarLint.stop();
        System.out.println(issueHandler.issues);
        return issueHandler.issues.stream()
                .filter(issue -> issue.getTextRange() != null)
                .map(ScannedViolation::new)
                .collect(Collectors.toList());
    }

    private static StandaloneAnalysisConfiguration getAnalysisConfigurationWithCliOptions(
            File projectRoot,
            List<JavaInputFile> inputFiles,
            List<Rule> rules,
            CLIConfigForStaticAnalyzer cliOptions) {
        return StandaloneAnalysisConfiguration.builder()
                .setBaseDir(projectRoot.toPath())
                // SonarLint takes classpath as a comma separated string to make it OS
                // independent.
                // See:
                // https://github.com/SonarSource/sonar-java/blob/6050868761069bc5ff965a149f2fd9a64d6319e0/sonar-java-plugin/src/main/resources/static/documentation.md#java-analysis-and-bytecode
                .putExtraProperty(
                        "sonar.java.libraries", String.join(",", cliOptions.getClasspath()))
                .addIncludedRules(
                        rules.stream()
                                .map(rule -> RuleKey.parse(String.format("java:%s", rule.getKey())))
                                .collect(Collectors.toList()))
                .addRuleParameters(
                        putRuleParameters(((SoraldConfig) cliOptions).getRuleParameters()))
                .addInputFiles(inputFiles)
                .build();
    }

    private static StandaloneAnalysisConfiguration getAnalysisConfigurationWithoutCliOptions(
            File projectRoot, List<JavaInputFile> inputFiles, List<Rule> rules) {
        return StandaloneAnalysisConfiguration.builder()
                .setBaseDir(projectRoot.toPath())
                .addIncludedRules(
                        rules.stream()
                                .map(rule -> RuleKey.parse(String.format("java:%s", rule.getKey())))
                                .collect(Collectors.toList()))
                .addInputFiles(inputFiles)
                .build();
    }

    private static Map<RuleKey, Map<String, String>> putRuleParameters(
            Map<Rule, Map<String, String>> passedRuleParameters) {
        Map<RuleKey, Map<String, String>> parsedRuleParameters = new HashMap<>();
        passedRuleParameters
                .keySet()
                .forEach(
                        rule -> {
                            parsedRuleParameters.put(
                                    RuleKey.parse(String.format("java:%s", rule.getKey())),
                                    passedRuleParameters.get(rule));
                        });
        return parsedRuleParameters;
    }

    private static class IssueHandler implements IssueListener {
        private final List<Issue> issues = new ArrayList<>();

        @Override
        public void handle(@Nonnull Issue issue) {
            issues.add(issue);
        }
    }
}
