package sonarquberepair;

import java.util.List;
import java.util.ArrayList;

/* All config settings of SonarQube should be gathered here */
public class SonarQubeRepairConfig {
	private final List<Integer> ruleKeys = new ArrayList<Integer>();
	private String projectKey;
	private PrettyPrintingStrategy prettyPrintingStrategy;
	private FileOutputStrategy fileOutputStrategy;
	private String originalFilesPath;
	private String workspace;
	private String gitRepoPath;

	public SonarQubeRepairConfig() {}

	public void setProjectKey(String projectKey) {
		this.projectKey = projectKey;
	}

	public String getProjectKey() {
		return this.projectKey;
	}

	public void addRuleKeys(int ruleNumber) {
		this.ruleKeys.add(ruleNumber);
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
}
