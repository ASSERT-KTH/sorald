package sonarquberepair;

public interface MainApi {
	void repair(String pathToFile, String projectKey, int ruleKey, PrettyPrintingStrategy prettyPrintingStrategy) throws Exception;

	void start(String[] args) throws Exception;
}