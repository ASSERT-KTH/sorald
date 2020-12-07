package sorald.event.collectors;

import java.util.*;
import sorald.event.SoraldEvent;
import sorald.event.SoraldEventHandler;
import sorald.event.models.miner.MinedRuleEvent;

/** Event handler for recording the miner mode statistics. */
public class MinerStatisticsCollector implements SoraldEventHandler {
    /** start of mining determined by currentTimeMillis */
    private long miningStartTime;

    /** start of mining determined by currentTimeMillis */
    private long miningEndTime;

    private List<MinedRuleEvent> minedRules = new ArrayList<>();

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
                MinedRuleEvent minedRuleEvent = (MinedRuleEvent) event;
                Optional<MinedRuleEvent> existingEvent =
                        minedRules.stream()
                                .filter(x -> x.getRuleKey().equals(minedRuleEvent.getRuleKey()))
                                .findFirst();

                if (!existingEvent.isEmpty()) {
                    existingEvent.get().addWarningLocations(minedRuleEvent.getWarningLocations());
                } else {
                    minedRules.add(minedRuleEvent);
                }

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
    public List<MinedRuleEvent> getMinedRules() {
        return Collections.unmodifiableList(minedRules);
    }
}
