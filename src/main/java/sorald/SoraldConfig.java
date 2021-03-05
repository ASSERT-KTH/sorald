package sorald;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import sorald.sonar.RuleViolation;

/* All config settings of Sorald should be gathered here */
public class SoraldConfig {
    private List<RuleViolation> ruleViolations;
    private PrettyPrintingStrategy prettyPrintingStrategy;
    private FileOutputStrategy fileOutputStrategy;
    private RepairStrategy repairStrategy;
    private String originalFilesPath;
    private String workspace;
    private String gitRepoPath;
    private int maxFixesPerRule;
    private int maxFilesPerSegment;
    private File statsOutputFile;

    public SoraldConfig() {}

    public void setRuleViolations(List<RuleViolation> ruleViolations) {
        this.ruleViolations = ruleViolations.stream().distinct().collect(Collectors.toList());
    }

    public List<RuleViolation> getRuleViolations() {
        return Collections.unmodifiableList(ruleViolations);
    }

    public void setPrettyPrintingStrategy(PrettyPrintingStrategy prettyPrintingStrategy) {
        this.prettyPrintingStrategy = prettyPrintingStrategy;
    }

    public PrettyPrintingStrategy getPrettyPrintingStrategy() {
        return this.prettyPrintingStrategy;
    }

    public void setFileOutputStrategy(FileOutputStrategy fileOutputStrategy) {
        this.fileOutputStrategy = fileOutputStrategy;
    }

    public FileOutputStrategy getFileOutputStrategy() {
        return this.fileOutputStrategy;
    }

    public void setRepairStrategy(RepairStrategy repairStrategy) {
        this.repairStrategy = repairStrategy;
    }

    public RepairStrategy getRepairStrategy() {
        return this.repairStrategy;
    }

    public void setOriginalFilesPath(String originalFilesPath) {
        this.originalFilesPath =
                Paths.get(originalFilesPath).normalize().toAbsolutePath().toString();
    }

    public String getOriginalFilesPath() {
        return this.originalFilesPath;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public String getWorkspace() {
        return this.workspace;
    }

    public void setGitRepoPath(String gitRepoPath) {
        this.gitRepoPath = gitRepoPath;
    }

    public String getGitRepoPath() {
        return this.gitRepoPath;
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
