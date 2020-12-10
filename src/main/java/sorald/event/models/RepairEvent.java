package sorald.event.models;

import sorald.event.EventType;
import sorald.event.SoraldEvent;

/**
 * Event representing a repair. This must be public for the json.org to be able to introspect it and
 * produce the nice JSON output.
 */
public class RepairEvent implements SoraldEvent {
    private final String ruleKey;
    private final String ruleViolationPosition;

    public RepairEvent(String ruleKey, String ruleViolationPosition) {
        this.ruleKey = ruleKey;
        this.ruleViolationPosition = ruleViolationPosition;
    }

    @Override
    public EventType type() {
        return EventType.REPAIR;
    }

    public String getRuleKey() {
        return ruleKey;
    }

    public String getRuleViolationPosition() {
        return ruleViolationPosition;
    }
}
