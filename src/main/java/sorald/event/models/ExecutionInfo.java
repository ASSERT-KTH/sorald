package sorald.event.models;

import java.util.List;

public class ExecutionInfo {
    private final List<String> originalArgs;
    private final String soraldVersion;
    private final String javaVersion;
    private final String target;

    public ExecutionInfo(
            List<String> originalArgs, String soraldVersion, String javaVersion, String target) {
        this.originalArgs = originalArgs;
        this.soraldVersion = soraldVersion;
        this.javaVersion = javaVersion;
        this.target = target;
    }

    public List<String> getOriginalArgs() {
        return originalArgs;
    }

    public String getSoraldVersion() {
        return soraldVersion;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public String getTarget() {
        return target;
    }
}
