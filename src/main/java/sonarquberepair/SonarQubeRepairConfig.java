package sonarquberepair;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import spoon.reflect.declaration.CtType;

/* All config settings of SonarQube should be gathered here */
public class SonarQubeRepairConfig {
	private final List<Integer> ruleNumbers = new ArrayList<Integer>();
	private String projectKey;
	private PrettyPrintingStrategy prettyPrintingStrategy;
	private OutputStrategy outputStrategy;
	private String repairPath;
	private String workspace;
	private String gitRepoPath;

	public SonarQubeRepairConfig() {}

	public void setProjectKey(String projectKey) {
		this.projectKey = projectKey;
	}

	public String getProjectKey() {
		return this.projectKey;
	}

	public void addRuleNumbers(int ruleNumber) {
		this.ruleNumbers.add(ruleNumber);
	}

	public List<Integer> getRuleNumbers() {
		return this.ruleNumbers;
	}

	public void setPrettyPrintingStrategy(PrettyPrintingStrategy prettyPrintingStrategy) {
		this.prettyPrintingStrategy = prettyPrintingStrategy;
	}

	public PrettyPrintingStrategy getPrettyPrintingStrategy() {
		return this.prettyPrintingStrategy;
	}

	public void setOutputStrategy(OutputStrategy outputStrategy) {
		this.outputStrategy = outputStrategy;
	}

	public OutputStrategy getOutputStrategy() {
		return this.outputStrategy;
	}

	public void setRepairPath(String repairPath) {
		this.repairPath = repairPath;
	}

	public String getRepairPath() {
		return this.repairPath;
	}

	public void setWorkSpace(String workspace) {
		this.workspace = workspace;
	}

	public String getWorkSpace() {
		return this.workspace;
	}

	public void setGitRepoPath(String gitRepoPath) {
		this.gitRepoPath = gitRepoPath;
	}

	public String getGitRepoPath() {
		return this.gitRepoPath;
	}
}
