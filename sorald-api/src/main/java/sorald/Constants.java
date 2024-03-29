package sorald;

import java.io.File;

public class Constants {
    private Constants() {}

    public static final String REPAIR_COMMAND_NAME = "repair";
    public static final String MINE_COMMAND_NAME = "mine";

    public static final String ARG_RULE_KEY = "--rule-key";
    public static final String ARG_RULE_KEYS = "--rule-keys";
    public static final String ARG_SOURCE = "--source";
    public static final String ARG_STATS_ON_GIT_REPOS = "--stats-on-git-repos";
    public static final String ARG_STATS_OUTPUT_FILE = "--stats-output-file";
    public static final String ARG_MINER_OUTPUT_FILE = "--miner-output-file";
    public static final String ARG_GIT_REPOS_LIST = "--git-repos-list";
    public static final String ARG_TEMP_DIR = "--temp-dir";
    public static final String ARG_PRETTY_PRINTING_STRATEGY = "--pretty-printing-strategy";
    public static final String ARG_MAX_FIXES_PER_RULE = "--max-fixes-per-rule";
    public static final String ARG_REPAIR_STRATEGY = "--repair-strategy";
    public static final String ARG_MAX_FILES_PER_SEGMENT = "--max-files-per-segment";
    public static final String ARG_RULE_TYPES = "--rule-types";
    public static final String ARG_HANDLED_RULES = "--handled-rules";
    public static final String ARG_RULE_VIOLATION_SPECIFIERS = "--violation-specs";
    public static final String ARG_TARGET = "--target";
    public static final String ARG_RESOLVE_CLASSPATH_FROM = "--resolve-classpath-from";
    public static final String ARG_RULE_PARAMETERS = "--rule-parameters";

    public static final String VIOLATION_SPECIFIER_SEP = File.pathSeparator;

    public static final String JAVA_EXT = ".java";

    public static final String LONG = "long";
    public static final String FLOAT = "float";
    public static final String DOUBLE = "double";

    public static final String STRING_QUALIFIED_NAME = "java.lang.String";
    public static final String JAVA_VERSION_SYSTEM_PROPERTY = "java.version";

    public static final String TOSTRING_METHOD_NAME = "toString";
    public static final String HASHCODE_METHOD_NAME = "hashCode";

    public static final String PATH_TO_STATS_OUTPUT = "experimentation/stats/warnings";

    public static final Integer DEFAULT_COMPLIANCE_LEVEL = 11;
}
