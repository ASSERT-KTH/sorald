package sorald.sonar;

import com.google.auto.service.AutoService;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonarsource.sonarlint.core.rule.extractor.SonarLintRuleDefinition;
import sorald.api.RuleRepository;
import sorald.rule.IRuleType;
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

    @Override
    public Collection<Rule> getRulesByType(IRuleType... types) {
        var ruleTypes = Set.of(types);
        return getAllRules().stream()
                .filter(rule -> ruleTypes.contains(rule.getType()))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Rule> getHandledRules() {
        SonarProcessorRepository sonarRepo = new SonarProcessorRepository();
        return getAllRules().stream()
                .filter(rule -> sonarRepo.getProcessor(rule.getKey()) != null)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Rule> getHandledRulesByType(IRuleType... types) {
        Set<IRuleType> ruleTypes = Set.of(types);
        return getHandledRules().stream()
                .filter(rule -> ruleTypes.contains(rule.getType()))
                .collect(Collectors.toList());
    }
}
