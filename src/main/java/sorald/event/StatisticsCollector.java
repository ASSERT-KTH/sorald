package sorald.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sorald.event.models.RepairEvent;

/** Event handler for Sorald that collects runtime statistics */
public class StatisticsCollector implements SoraldEventHandler {
    private long parseStart = -1;
    private long parseEnd = -1;
    private long repairStart = -1;
    private long repairEnd = -1;
    private final List<SoraldEvent> crashes = new ArrayList<>();

    private final Map<String, List<RepairEvent>> keysToRepairEvents = new HashMap<>();

    @Override
    public void registerEvent(SoraldEvent event) {
        switch (event.type()) {
            case PARSE_START:
                parseStart = System.nanoTime();
                break;
            case PARSE_END:
                parseEnd = System.nanoTime();
                break;
            case REPAIR_START:
                repairStart = System.nanoTime();
                break;
            case REPAIR_END:
                repairEnd = System.nanoTime();
                break;
            case REPAIR:
                addRepair((RepairEvent) event);
                break;
            case CRASH:
                crashes.add(event);
                break;
        }
    }

    private void addRepair(RepairEvent event) {
        keysToRepairEvents.putIfAbsent(event.getRuleKey(), new ArrayList<>());
        keysToRepairEvents.get(event.getRuleKey()).add(event);
    }

    /** @return The total amount of time spent parsing */
    public long getParseTimeNs() {
        return parseEnd - parseStart;
    }

    /** @return The total amount of time spent repairing */
    public long getRepairTimeNs() {
        return repairEnd - repairStart;
    }

    /** @return All repair event data */
    public Map<String, List<RepairEvent>> getRepairs() {
        return Collections.unmodifiableMap(keysToRepairEvents);
    }

    /** @return All crash event data */
    public List<SoraldEvent> getCrashes() {
        return Collections.unmodifiableList(crashes);
    }
}
