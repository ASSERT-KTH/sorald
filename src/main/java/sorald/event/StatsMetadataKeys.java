package sorald.event;

/** Metadata keys used for the stats JSON file. */
public class StatsMetadataKeys {
    // General data
    public static final String REPAIRS = "repairs";
    public static final String ORIGINAL_ARGS = "originalArgs";
    public static final String PARSE_TIME_MS = "parseTimeMs";
    public static final String REPAIR_TIME_MS = "repairTimeMs";
    public static final String TOTAL_TIME_MS = "totalTimeMs";
    public static final String START_TIME_MS = "startTimeMs";
    public static final String END_TIME_MS = "endTimeMs";
    public static final String VIOLATION_SPECIFIER = "violationSpecifier";
    public static final String CRASHES = "crashes";

    public static final String EXECUTION_INFO = "executionInfo";
    public static final String SORALD_VERSION = "soraldVersion";
    public static final String JAVA_VERSION = "javaVersion";

    // Repair-specific data
    public static final String REPAIR_RULE_KEY = "ruleKey";
    public static final String REPAIR_RULE_NAME = "ruleName";
    public static final String REPAIR_PERFORMED_LOCATIONS = "performedRepairsLocations";
    public static final String REPAIR_CRASHED_LOCATIONS = "crashedRepairsLocations";
    public static final String REPAIR_NB_FAILURES = "nbCrashedRepairs";
    public static final String REPAIR_NB_VIOLATIONS_BEFORE = "nbViolationsBefore";
    public static final String REPAIR_NB_VIOLATIONS_AFTER = "nbViolationsAfter";
    public static final String REPAIR_NB_PERFORMED = "nbPerformedRepairs";

    // Mining-specific data
    public static final String MINING_START_TIME = "miningStartTime";
    public static final String MINING_END_TIME = "miningEndTime";
    public static final String MINED_RULES = "minedRules";
    public static final String TOTAL_MINING_TIME = "totalMiningTime";
}
