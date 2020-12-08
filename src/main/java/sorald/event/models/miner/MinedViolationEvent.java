package sorald.event.models.miner;

import sorald.event.EventType;
import sorald.event.SoraldEvent;
import sorald.event.models.WarningLocation;
import sorald.sonar.RuleViolation;

/** Event representing a mined rule information */
public class MinedViolationEvent implements SoraldEvent {
    private final String ruleKey;
    private final String ruleName;
    private final WarningLocation warningLocation;

    /** Wrapping just one violation inside a mined-rule event */
    public MinedViolationEvent(RuleViolation violation) {
        this.ruleKey = violation.getRuleKey();
        this.ruleName = violation.getCheckName();
        this.warningLocation = new WarningLocation(violation);
    }

    public String getRuleKey() {
        return ruleKey;
    }

    public String getRuleName() {
        return ruleName;
    }

    @Override
    public EventType type() {
        return EventType.MINED;
    }

    public WarningLocation getWarningLocation() {
        return warningLocation;
    }
}
