package sorald.cli;

import java.util.List;

/** Stores CLI options that is needed by {@link sorald.rule.StaticAnalyzer}. */
public interface CLIConfigForStaticAnalyzer {
    List<String> getClasspath();

    CLIConfigForStaticAnalyzer setClasspath(List<String> classpath);
}
