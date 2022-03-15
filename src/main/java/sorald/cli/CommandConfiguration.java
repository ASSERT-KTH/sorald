package sorald.cli;

import java.util.List;
import sorald.rule.Rule;
import sorald.rule.RuleType;

public class CommandConfiguration {
    private Boolean handledRules = null;
    private List<RuleType> ruleTypes = null;

    private List<Rule> rules = null;

    public CommandConfiguration(boolean handledRules, List<RuleType> ruleTypes) {
        this.handledRules = handledRules;
        this.ruleTypes = ruleTypes;
    }

    public CommandConfiguration(List<Rule> rules) {
        this.rules = rules;
    }

    public Boolean onlyHandledRules() {
        return handledRules;
    }

    public List<RuleType> getRuleTypes() {
        return ruleTypes;
    }

    public List<Rule> getRules() {
        return rules;
    }
}
