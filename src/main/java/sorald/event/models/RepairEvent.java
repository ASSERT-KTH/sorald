package sorald.event.models;

import sorald.event.EventType;
import sorald.event.SoraldEvent;
import sorald.sonar.RuleViolation;

/**
 * Event representing a repair. This must be public for the json.org to be able to introspect it and
 * produce the nice JSON output.
 */
public class RepairEvent implements SoraldEvent {
    private final String ruleKey;
    private final RuleViolation ruleViolation;

    public RepairEvent(String ruleKey, RuleViolation ruleViolation) {
        this.ruleKey = ruleKey;
        this.ruleViolation = ruleViolation;
    }

    @Override
    public EventType type() {
        return EventType.REPAIR;
    }

    public String getRuleKey() {
        return ruleKey;
    }

    public RuleViolation getRuleViolation() {
        return ruleViolation;
    }
}
