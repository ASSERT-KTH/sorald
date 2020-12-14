package sorald.event.collectors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sorald.event.SoraldEvent;
import sorald.event.SoraldEventHandler;
import sorald.event.models.RepairEvent;
import sorald.event.models.miner.MinedViolationEvent;

/**
 * Event handler for Sorald that collects runtime statistics during execution of the repair command.
 */
public class RepairStatisticsCollector implements SoraldEventHandler {
    private static final int INVALID_TIME = -1;

    private long execStart = INVALID_TIME;
    private long execEnd = INVALID_TIME;
    private long parseStart = INVALID_TIME;
    private long repairStart = INVALID_TIME;
    private long parseTotal = 0;
    private long repairTotal = 0;
    private final List<SoraldEvent> crashes = new ArrayList<>();

    private final Map<String, List<RepairEvent>> keyToRepairs = new HashMap<>();
    private final Map<String, List<RepairEvent>> keyToFailures = new HashMap<>();
    private final Map<String, List<MinedViolationEvent>> minedViolationsBefore = new HashMap<>();
    private final Map<String, List<MinedViolationEvent>> minedViolationsAfter = new HashMap<>();

    @Override
    public void registerEvent(SoraldEvent event) {
        switch (event.type()) {
            case EXEC_START:
                execStart = System.currentTimeMillis();
                break;
            case EXEC_END:
                execEnd = System.currentTimeMillis();
                break;
            case PARSE_START:
                parseStart = System.currentTimeMillis();
                break;
            case PARSE_END:
                assert parseStart != INVALID_TIME;
                long parseEnd = System.currentTimeMillis();
                parseTotal += parseEnd - parseStart;
                parseStart = INVALID_TIME;
                break;
            case REPAIR_START:
                repairStart = System.currentTimeMillis();
                break;
            case REPAIR_END:
                assert repairStart != INVALID_TIME;
                long repairEnd = System.currentTimeMillis();
                repairTotal += repairEnd - repairStart;
                repairStart = INVALID_TIME;
                break;
            case REPAIR:
                addRepair((RepairEvent) event);
                break;
            case CRASH:
                crashes.add(event);
                break;
            case MINED:
                var mined = (MinedViolationEvent) event;

                if (execEnd == INVALID_TIME) {
                    // we have not yet reached end of execution, so this is a before-warning
                    addToEventMap(mined.getRuleKey(), mined, minedViolationsBefore);
                } else {
                    // end of execution has been reached, so this is an after-warning
                    addToEventMap(mined.getRuleKey(), mined, minedViolationsAfter);
                }
                break;
        }
    }

    private void addRepair(RepairEvent event) {
        var map = event.isFailure() ? keyToFailures : keyToRepairs;
        addToEventMap(event.getRuleKey(), event, map);
    }

    private <T extends SoraldEvent> void addToEventMap(
            String key, T event, Map<String, List<T>> eventsMap) {
        eventsMap.putIfAbsent(key, new ArrayList<>());
        eventsMap.get(key).add(event);
    }

    /** @return All repair events that were performed without errors. */
    public Map<String, List<RepairEvent>> performedRepairs() {
        return Collections.unmodifiableMap(keyToRepairs);
    }

    /** @return All repair events that crashed during execution. */
    public Map<String, List<RepairEvent>> crashedRepairs() {
        return Collections.unmodifiableMap(keyToFailures);
    }

    /** @return Mapping from key to all warnings mined for that key before repairs. */
    public Map<String, List<MinedViolationEvent>> minedViolationsBefore() {
        return Collections.unmodifiableMap(minedViolationsBefore);
    }

    /** @return Mapping from key to all warnings mined for that key after repairs. */
    public Map<String, List<MinedViolationEvent>> minedViolationsAfter() {
        return Collections.unmodifiableMap(minedViolationsAfter);
    }

    /** @return All crash event data */
    public List<SoraldEvent> getCrashes() {
        return Collections.unmodifiableList(crashes);
    }

    /** @return The total amount of time spent parsing */
    public long getParseTimeMs() {
        return parseTotal;
    }

    /** @return The total amount of time spent repairing */
    public long getRepairTimeMs() {
        return repairTotal;
    }

    /** @return The total amount of execution time in milliseconds. */
    public long getTotalTimeMs() {
        return execEnd - execStart;
    }

    /** @return The start time of execution in milliseconds from the unix epoch. */
    public long getStartTimeMs() {
        return execStart;
    }

    /** @return The end time of execution in milliseconds from the unix epoch. */
    public long getEndTimeMs() {
        return execEnd;
    }
}
