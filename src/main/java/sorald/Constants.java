package sorald;

import java.io.File;
import java.util.Collections;
import java.util.List;
import org.sonar.plugins.java.api.JavaFileScanner;
import sorald.sonar.Checks;

public class Constants {
    public static final String SORALD_VERSION = "92d377cc39f63be35f9bee00d372c8f1ca06be5e";

    public static final String REPAIR_COMMAND_NAME = "repair";
    public static final String MINE_COMMAND_NAME = "mine";

    public static final String ARG_RULE_KEYS = "--ruleKeys";
    public static final String ARG_ORIGINAL_FILES_PATH = "--originalFilesPath";
    public static final String ARG_STATS_ON_GIT_REPOS = "--statsOnGitRepos";
    public static final String ARG_STATS_OUTPUT_FILE = "--statsOutputFile";
    public static final String ARG_MINER_OUTPUT_FILE = "--minerOutputFile";
    public static final String ARG_GIT_REPOS_LIST = "--gitReposList";
    public static final String ARG_TEMP_DIR = "--tempDir";
    public static final String ARG_WORKSPACE = "--workspace";
    public static final String ARG_GIT_REPO_PATH = "--gitRepoPath";
    public static final String ARG_PRETTY_PRINTING_STRATEGY = "--prettyPrintingStrategy";
    public static final String ARG_FILE_OUTPUT_STRATEGY = "--fileOutputStrategy";
    public static final String ARG_MAX_FIXES_PER_RULE = "--maxFixesPerRule";
    public static final String ARG_REPAIR_STRATEGY = "--repairStrategy";
    public static final String ARG_MAX_FILES_PER_SEGMENT = "--maxFilesPerSegment";
    public static final String ARG_RULE_TYPES = "--ruleTypes";
    public static final String ARG_RULE_VIOLATION_SPECIFIERS = "--violationSpecs";
    public static final String ARG_TARGET = "--target";

    public static final String PROCESSOR_PACKAGE = "sorald.processor";

    public static final String VIOLATION_SPECIFIER_SEP = File.pathSeparator;

    public static final String SORALD_WORKSPACE = "sorald-workspace";
    public static final String PATCHES = "SoraldGitPatches";
    public static final String PATCH_FILE_PREFIX = "soraldpatch_";
    public static final String PATH_TO_RESOURCES_FOLDER = "./src/test/resources/";

    public static final String JAVA_EXT = ".java";
    public static final String PATCH_EXT = ".patch";

    public static final String SPOONED = "spooned";
    public static final String INTERMEDIATE = "intermediate";
    public static final String SPOONED_INTERMEDIATE = SPOONED + File.separator + INTERMEDIATE;

    public static final String INT = "int";
    public static final String LONG = "long";
    public static final String FLOAT = "float";
    public static final String DOUBLE = "double";

    public static final String STRING_QUALIFIED_NAME = "java.lang.String";
    public static final String JAVA_VERSION_SYSTEM_PROPERTY = "java.version";

    public static final String TOSTRING_METHOD_NAME = "toString";
    public static final String HASHCODE_METHOD_NAME = "hashCode";

    public static final String PATH_TO_STATS_OUTPUT = "experimentation/stats/warnings";

    public static final List<Class<? extends JavaFileScanner>> SONAR_CHECK_CLASSES;

    static {
        SONAR_CHECK_CLASSES = Collections.unmodifiableList(Checks.getAllChecks());
    }
}
