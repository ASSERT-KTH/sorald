package sorald;

import java.util.ArrayList;
import java.util.List;

/* All config settings of Sorald should be gathered here */
public class SoraldConfig {
	private final List<Integer> ruleKeys = new ArrayList<>();
	private PrettyPrintingStrategy prettyPrintingStrategy;
	private FileOutputStrategy fileOutputStrategy;
	private String originalFilesPath;
	private String workspace;
	private String gitRepoPath;
	private int maxFixesPerRule;

	public SoraldConfig() {}

	public void addRuleKeys(List<Integer> ruleKeys) {
		this.ruleKeys.addAll(ruleKeys);
	}

	public List<Integer> getRuleKeys() {
		return this.ruleKeys;
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

	public void setOriginalFilesPath(String originalFilesPath) {
		this.originalFilesPath = originalFilesPath;
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
}
