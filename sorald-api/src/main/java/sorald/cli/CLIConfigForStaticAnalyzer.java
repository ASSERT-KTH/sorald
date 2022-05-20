package sorald.cli;

import java.util.List;

public interface CLIConfigForStaticAnalyzer {
    List<String> getClasspath();

    CLIConfigForStaticAnalyzer setClasspath(List<String> classpath);
}
