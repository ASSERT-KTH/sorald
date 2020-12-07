package sorald.event.models.miner;

import java.util.ArrayList;
import java.util.List;
import sorald.event.EventType;
import sorald.event.SoraldEvent;
import sorald.event.models.WarningLocation;
import sorald.sonar.RuleViolation;

/** Event representing a mined rule information */
public class MinedRuleEvent implements SoraldEvent {
    private final String ruleKey;
    private final String ruleName;
    private final Integer nbFoundWarnings;
    private final List<WarningLocation> warningLocations;

    public MinedRuleEvent(String ruleKey, String ruleName, Integer nbFoundWarnings) {
        this.ruleName = ruleName;
        this.ruleKey = ruleKey;
        this.nbFoundWarnings = nbFoundWarnings;
        this.warningLocations = new ArrayList<>();
    }

    /** Wrapping just one violation inside a mined-rule event */
    public MinedRuleEvent(RuleViolation violation) {
        this.ruleKey = violation.getRuleKey();
        this.ruleName = violation.getCheckName();
        this.nbFoundWarnings = 1;
        this.warningLocations = new ArrayList<>();
        this.warningLocations.add(new WarningLocation(violation));
    }

    public void addWarningLocations(List<WarningLocation> newLocations) {
        warningLocations.addAll(newLocations);
    }

    public String getRuleKey() {
        return ruleKey;
    }

    public String getRuleName() {
        return ruleName;
    }

    public Integer getNbFoundWarnings() {
        return nbFoundWarnings;
    }

    public List<WarningLocation> getWarningLocations() {
        return warningLocations;
    }

    @Override
    public EventType type() {
        return EventType.MINED;
    }
}
