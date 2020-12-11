package sorald.event;

/** Metadata keys used for the stats JSON file. */
public class StatsMetadataKeys {
    // General data
    public static final String REPAIRS = "repairs";
    public static final String ORIGINAL_ARGS = "originalArgs";
    public static final String PARSE_TIME_NS = "parseTimeNs";
    public static final String REPAIR_TIME_NS = "repairTimeNs";

    public static final String EXECUTION_INFO = "executionInfo";
    public static final String SORALD_VERSION = "soraldVersion";
    public static final String JAVA_VERSION = "javaVersion";

    // Repair-specific data
    public static final String REPAIR_RULE_KEY = "ruleKey";
    public static final String REPAIR_RULE_NAME = "ruleName";
    public static final String REPAIR_PERFORMED_LOCATIONS = "performedRepairsLocations";
    public static final String REPAIR_CRASHED_LOCATIONS = "crashedRepairsLocations";
    public static final String REPAIR_NB_FAILURES = "nbCrashedRepairs";
    public static final String REPAIR_NB_WARNINGS = "nbFoundWarnings";
    public static final String REPAIR_NB_PERFORMED = "nbPerformedRepairs";

    // Mining-specific data
    public static final String MINING_START_TIME = "miningStartTime";
    public static final String MINING_END_TIME = "miningEndTime";
    public static final String MINED_RULES = "minedRules";
    public static final String TOTAL_MINING_TIME = "totalMiningTime";
}
