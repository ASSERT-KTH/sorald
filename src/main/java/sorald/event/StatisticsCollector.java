package sorald.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Event handler for Sorald that collects runtime statistics */
public class StatisticsCollector implements SoraldEventHandler {
    private long parseStart = -1;
    private long parseEnd = -1;
    private long repairStart = -1;
    private long repairEnd = -1;
    private final List<SoraldEvent> repairs = new ArrayList<>();

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
                repairs.add(event);
                break;
            default:
                // do nothing
        }
    }

    /** @return The total amount of time spent parsing */
    public long getParseTimeNs() {
        assert parseEnd > parseStart;
        return parseEnd - parseStart;
    }

    /** @return The total amount of time spent repairing */
    public long getRepairTimeNs() {
        assert repairEnd > repairStart;
        return repairEnd - repairStart;
    }

    /** @return All repair event data */
    public List<SoraldEvent> getRepairs() {
        return Collections.unmodifiableList(repairs);
    }
}
