package sorald;

import java.io.File;
import java.nio.file.Paths;
import java.util.Optional;

/* All config settings of Sorald should be gathered here */
public class SoraldConfig {
    private PrettyPrintingStrategy prettyPrintingStrategy;
    private RepairStrategy repairStrategy;
    private String source;
    private int maxFixesPerRule;
    private int maxFilesPerSegment;
    private File statsOutputFile;

    public SoraldConfig() {}

    public void setPrettyPrintingStrategy(PrettyPrintingStrategy prettyPrintingStrategy) {
        this.prettyPrintingStrategy = prettyPrintingStrategy;
    }

    public PrettyPrintingStrategy getPrettyPrintingStrategy() {
        return this.prettyPrintingStrategy;
    }

    public void setRepairStrategy(RepairStrategy repairStrategy) {
        this.repairStrategy = repairStrategy;
    }

    public RepairStrategy getRepairStrategy() {
        return this.repairStrategy;
    }

    public void setSource(String source) {
        this.source = Paths.get(source).normalize().toAbsolutePath().toString();
    }

    public String getSource() {
        return this.source;
    }

    public void setMaxFixesPerRule(int maxFixesPerRule) {
        this.maxFixesPerRule = maxFixesPerRule;
    }

    public int getMaxFixesPerRule() {
        return this.maxFixesPerRule;
    }

    public void setMaxFilesPerSegment(int maxFilesPerSegment) {
        this.maxFilesPerSegment = maxFilesPerSegment;
    }

    public int getMaxFilesPerSegment() {
        return this.maxFilesPerSegment;
    }

    public void setStatsOutputFile(File statsOutputFile) {
        this.statsOutputFile = statsOutputFile;
    }

    public Optional<File> getStatsOutputFile() {
        return Optional.ofNullable(statsOutputFile);
    }
}
