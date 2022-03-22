package sorald.sonar;

import java.util.Objects;
import org.sonarsource.sonarlint.core.rule.extractor.SonarLintRuleDefinition;
import sorald.rule.Rule;
import sorald.rule.RuleType;

public class SonarRule implements Rule {
    private final String key;
    private final String name;
    private final RuleType type;

    private static final String SONAR_JAVA_PREFIX = "java:";

    public SonarRule(String key) {
        this.key = withoutLanguage(key);

        SonarLintRuleDefinition ruleDefinition =
                SonarLintEngine.getAllRulesDefinitionsByKey().get(withLanguage(key));
        this.name = ruleDefinition.getName();
        this.type = RuleType.valueOf(ruleDefinition.getType());
    }

    private static String withoutLanguage(String ruleKey) {
        if (ruleKey.contains(SONAR_JAVA_PREFIX)) {
            return ruleKey.substring(5);
        }
        return ruleKey;
    }

    private static String withLanguage(String ruleKey) {
        if (ruleKey.contains(SONAR_JAVA_PREFIX)) {
            return ruleKey;
        }
        return SONAR_JAVA_PREFIX + ruleKey;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public RuleType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SonarRule sonarRule = (SonarRule) o;
        return key.equals(sonarRule.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
