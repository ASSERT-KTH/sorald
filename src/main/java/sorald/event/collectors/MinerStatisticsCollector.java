package sorald.event.collectors;

import com.google.common.collect.ImmutableList;
import java.util.*;
import java.util.stream.Collectors;
import sorald.event.SoraldEvent;
import sorald.event.SoraldEventHandler;
import sorald.event.models.WarningLocation;
import sorald.event.models.miner.MinedRule;
import sorald.event.models.miner.MinedViolationEvent;

/** Event handler for recording the miner mode statistics. */
public class MinerStatisticsCollector implements SoraldEventHandler {
    private static final String RULE_ID_SEPARATOR = ":";

    /** start of mining determined by currentTimeMillis */
    private long miningStartTime;

    /** start of mining determined by currentTimeMillis */
    private long miningEndTime;

    private Map<String, List<WarningLocation>> ruleToViolations = new HashMap<>();

    @Override
    public void registerEvent(SoraldEvent event) {
        switch (event.type()) {
            case MINING_START:
                miningStartTime = System.currentTimeMillis();
                break;
            case MINING_END:
                miningEndTime = System.currentTimeMillis();
                break;
            case MINED:
                MinedViolationEvent minedViolationEvent = (MinedViolationEvent) event;

                if (!ruleToViolations.containsKey(violationToRuleId(minedViolationEvent)))
                    ruleToViolations.put(violationToRuleId(minedViolationEvent), new ArrayList<>());

                ruleToViolations
                        .get(violationToRuleId(minedViolationEvent))
                        .add(minedViolationEvent.getWarningLocation());
                break;
        }
    }

    /** @return The start time of mining */
    public Date getMiningStartTime() {
        return new Date(miningStartTime);
    }

    /** @return The end time of mining */
    public Date getMiningEndTime() {
        return new Date(miningEndTime);
    }

    /** @return The duration of mining in millis */
    public long getTotalMiningTime() {
        return miningEndTime - miningStartTime;
    }

    /** @return All mined rules data */
    public List<MinedRule> getMinedRules() {
        return ruleToViolations.entrySet().stream()
                .map(
                        e ->
                                new MinedRule(
                                        e.getKey().split(RULE_ID_SEPARATOR)[0],
                                        e.getKey().split(RULE_ID_SEPARATOR)[1],
                                        ImmutableList.copyOf(e.getValue())))
                .collect(Collectors.toList());
    }

    private String violationToRuleId(MinedViolationEvent violation) {
        return violation.getRuleKey() + RULE_ID_SEPARATOR + violation.getRuleName();
    }
}
