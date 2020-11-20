package sorald.event;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class StatisticsCollector implements SoraldEventHandler {
    private long parseStart = -1;
    private long parseEnd = -1;
    private long repairStart = -1;
    private long repairEnd = -1;
    private EnumMap<EventType, List<EventMetadata>> allMetadata =
            new EnumMap<>(EventType.class);

    @Override
    public void registerEvent(EventType eventType) {
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
        registerEvent(type);
        List<EventMetadata> eventTypeMetadata =
                allMetadata.getOrDefault(type, new ArrayList<>());
        eventTypeMetadata.add(metadata);
        allMetadata.putIfAbsent(type, eventTypeMetadata);
    }

    @Override
    public void close() {
        System.out.println(
                "Time to parse: "
                        + (parseEnd - parseStart) / 1_000_000
                        + " ms");
        System.out.println(
                "Time to repair: "
                        + (repairEnd - repairStart) / 1_000_000
                        + " ms");
        System.out.println(allMetadata);
    }
}
