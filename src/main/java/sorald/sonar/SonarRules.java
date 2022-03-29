package sorald.sonar;

import com.google.auto.service.AutoService;
import java.util.Collection;
import java.util.stream.Collectors;
import org.sonarsource.sonarlint.core.rule.extractor.SonarLintRuleDefinition;
import sorald.api.RuleRepository;
import sorald.rule.Rule;

/** Class that knows about all Sonar rules. Should ONLY be used by {@link sorald.rule.Rules}. */
@AutoService(RuleRepository.class)
public class SonarRules implements RuleRepository {

    /**
     * Get all SonarJava rules.
     *
     * @return All SonarJava rules.
     */
    public Collection<Rule> getAllRules() {
        Collection<SonarLintRuleDefinition> allRules =
                SonarLintEngine.getAllRulesDefinitionsByKey().values();

        return allRules.stream().map(slrd -> Rule.of(slrd.getKey())).collect(Collectors.toList());
    }
}
