package sorald.qodana.rules;

import sorald.rule.IRuleType;
import sorald.rule.Rule;

public enum QodanaRules implements Rule {
    STRING_OPERATION_CAN_BE_SIMPLIFIED(
            new QodanaType(), "StringOperationCanBeSimplified", "StringOperationCanBeSimplified");

    private IRuleType ruleType;
    private String key;
    private String name;

    QodanaRules(IRuleType type, String key, String name) {
        this.ruleType = type;
        this.key = key;
        this.name = name;
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
    public IRuleType getType() {
        return ruleType;
    }

    private static class QodanaType implements IRuleType {
        @Override
        public String getName() {
            return "Qodana";
        }
    }
}
