package sorald.qodana;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import sorald.api.RuleRepository;
import sorald.qodana.rules.QodanaRules;
import sorald.rule.IRuleType;
import sorald.rule.Rule;

public class QodanaRuleRepository implements RuleRepository {

    @Override
    public Collection<Rule> getAllRules() {
        return Arrays.asList(QodanaRules.values());
    }

    @Override
    public Collection<Rule> getRulesByType(IRuleType... types) {
        Set<IRuleType> ruleTypes = new HashSet<>(Arrays.asList(types));
        return getAllRules().stream()
                .filter(v -> ruleTypes.contains(v.getType()))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Rule> getHandledRules() {
        return getAllRules();
    }

    @Override
    public Collection<Rule> getHandledRulesByType(IRuleType... types) {
        return getRulesByType(types);
    }
}
