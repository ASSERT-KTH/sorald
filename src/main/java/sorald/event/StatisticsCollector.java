package sorald.event;

import sorald.event.EventMetadata;
import sorald.event.EventType;
import sorald.event.SoraldEventHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

/** Event handler for Sorald that collects runtime statistics */
public class StatisticsCollector implements SoraldEventHandler {
    private boolean eventRegistered = false;
    private long parseStart = -1;
    private long parseEnd = -1;
    private long repairStart = -1;
    private long repairEnd = -1;
    private EnumMap<EventType, List<EventMetadata>> allMetadata = new EnumMap<>(EventType.class);

    @Override
    public void registerEvent(EventType eventType) {
        eventRegistered = true;
        switch (eventType) {
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
            default:
                // do nothing
        }
    }

    @Override
    public void registerEvent(EventType type, EventMetadata metadata) {
        eventRegistered = true;
        List<EventMetadata> eventTypeMetadata = allMetadata.getOrDefault(type, new ArrayList<>());
        eventTypeMetadata.add(metadata);
        allMetadata.putIfAbsent(type, eventTypeMetadata);
    }

    /**
     * @return True iff at least one event was registered by this handler
     */
    public boolean isEventRegistered() {
        return eventRegistered;
    }

    /**
     * @return The total amount of time spent parsing
     */
    public long getParseTimeNs() {
        assert parseEnd > parseStart;
        return parseEnd - parseStart;
    }

    /**
     * @return The total amount of time spent repairing
     */
    public long getRepairTimeNs() {
        assert repairEnd > repairStart;
        return repairEnd - repairStart;
    }

    /**
     * @return All repair event data
     */
    public List<EventMetadata> getRepairs() {
        return Collections.unmodifiableList(allMetadata.getOrDefault(EventType.REPAIR, List.of()));
    }
}
