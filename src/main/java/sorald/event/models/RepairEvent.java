package sorald.event.models;

import sorald.event.EventType;
import sorald.event.SoraldEvent;
import sorald.sonar.RuleViolation;

/**
 * Event representing a repair. This must be public for the json.org to be able to introspect it and
 * produce the nice JSON output.
 */
public class RepairEvent implements SoraldEvent {
    private final RuleViolation ruleViolation;
    private final boolean failure;

    // TODO document
    public RepairEvent(RuleViolation ruleViolation, boolean failure) {
        this.ruleViolation = ruleViolation;
        this.failure = failure;
    }

    @Override
    public EventType type() {
        return EventType.REPAIR;
    }

    public String getRuleKey() {
        return ruleViolation.getRuleKey();
    }

    public boolean isFailure() {
        return failure;
    }

    public RuleViolation getRuleViolation() {
        return ruleViolation;
    }
}
