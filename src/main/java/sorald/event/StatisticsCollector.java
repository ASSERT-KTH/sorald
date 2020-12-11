package sorald.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sorald.event.models.RepairEvent;
import sorald.event.models.miner.MinedViolationEvent;

/** Event handler for Sorald that collects runtime statistics */
public class StatisticsCollector implements SoraldEventHandler {
    private long execStart = -1;
    private long execEnd = -1;
    private long parseStart = -1;
    private long repairStart = -1;
    private long parseTotal = 0;
    private long repairTotal = 0;
    private final List<SoraldEvent> crashes = new ArrayList<>();

    private final Map<String, List<RepairEvent>> keyToRepairs = new HashMap<>();
    private final Map<String, List<RepairEvent>> keyToFailures = new HashMap<>();
    private final Map<String, List<MinedViolationEvent>> minedWarnings = new HashMap<>();

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
                assert parseStart > 0;
                long parseEnd = System.currentTimeMillis();
                parseTotal += parseEnd - parseStart;
                parseStart = -1;
                break;
            case REPAIR_START:
                repairStart = System.currentTimeMillis();
                break;
            case REPAIR_END:
                assert repairStart > 0;
                long repairEnd = System.nanoTime();
                repairTotal += repairEnd - repairStart;
                repairStart = -1;
                break;
            case REPAIR:
                addRepair((RepairEvent) event);
                break;
            case CRASH:
                crashes.add(event);
                break;
            case MINED:
                var mined = (MinedViolationEvent) event;
                addToEventMap(mined.getRuleKey(), mined, minedWarnings);
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

    /** @return The total amount of time spent parsing */
    public long getParseTimeMs() {
        return parseTotal;
    }

    /** @return The total amount of time spent repairing */
    public long getRepairTimeMs() {
        return repairTotal;
    }

    /** @return All repair events that were performed without errors. */
    public Map<String, List<RepairEvent>> performedRepairs() {
        return Collections.unmodifiableMap(keyToRepairs);
    }

    /** @return All repair events that crashed during execution. */
    public Map<String, List<RepairEvent>> crashedRepairs() {
        return Collections.unmodifiableMap(keyToFailures);
    }

    public Map<String, List<MinedViolationEvent>> minedWarnings() {
        return Collections.unmodifiableMap(minedWarnings);
    }

    /** @return All crash event data */
    public List<SoraldEvent> getCrashes() {
        return Collections.unmodifiableList(crashes);
    }

    /** The total amount of execution time. */
    public long getTotalTimeMs() {
        return execEnd - execStart;
    }

    /** The start time in unix epoch. */
    public long getStartTimeMs() {
        return execStart / 1000L;
    }

    /** The end time in unix epoch. */
    public long getEndTimeMs() {
        return execEnd / 1000L;
    }
}
