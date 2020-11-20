package sorald.event;

/** Metadata keys used for the stats JSON file. */
public class StatsMetadataKeys {
    // General data
    public static final String REPAIRS = "repairs";
    public static final String ORIGINAL_ARGS = "originalArgs";
    public static final String PARSE_TIME_NS = "parseTimeNs";
    public static final String REPAIR_TIME_NS = "repairTimeNs";

    // Repair-specific data
    public static final String REPAIR_RULE_KEY = "ruleKey";
    public static final String REPAIR_RULE_VIOLATION_POSITION = "ruleViolationPosition";
}
