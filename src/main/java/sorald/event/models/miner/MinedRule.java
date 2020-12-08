package sorald.event.models.miner;

import com.google.common.collect.ImmutableList;
import sorald.event.models.WarningLocation;

public class MinedRule {
    private final String ruleKey;
    private final String ruleName;
    private final ImmutableList<WarningLocation> warningLocations;

    public MinedRule(
            String ruleKey, String ruleName, ImmutableList<WarningLocation> warningLocations) {
        this.ruleKey = ruleKey;
        this.ruleName = ruleName;
        this.warningLocations = warningLocations;
    }

    public ImmutableList<WarningLocation> getWarningLocations() {
        return warningLocations;
    }

    public String getRuleName() {
        return ruleName;
    }

    public String getRuleKey() {
        return ruleKey;
    }
}
