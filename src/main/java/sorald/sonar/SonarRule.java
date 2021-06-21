package sorald.sonar;

import java.util.Objects;
import sorald.rule.Rule;

public class SonarRule implements Rule {
    private final String key;
    private final String name;

    public SonarRule(String key) {
        this.key = key;
        this.name =
                Checks.getAllChecks().stream()
                        .filter(check -> Checks.getRuleKey(check).equals(key))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "No Sonar rule with key: " + key))
                        .getSimpleName()
                        .replaceFirst("Check$", "");
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
