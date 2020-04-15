package sonarquberepair;

import spoon.support.sniper.SniperJavaPrettyPrinter;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;


public class Main {
	private final SonarQubeRepairConfig config = new SonarQubeRepairConfig();

	public SonarQubeRepairConfig getConfig() {
		return this.config;
	}

	public JSAP defineArgs() throws JSAPException{
		JSAP jsap = new JSAP();

		/* will be supporting multiple rules processing later so rulekey(s) */
		FlaggedOption opt = new FlaggedOption("ruleKeys");
		opt.setLongFlag("ruleKeys");
		opt.setStringParser(JSAP.INTEGER_PARSER);
		opt.setDefault("2116");
		opt.setHelp("Sonarqube rule key, Check https://rules.sonarsource.com/java");
		jsap.registerParameter(opt);

		opt = new FlaggedOption("projectKey");
		opt.setLongFlag("projectKey");
		opt.setStringParser(JSAP.STRING_PARSER);
		opt.setDefault("fr.inria.gforge.spoon:spoon-core");
		opt.setHelp("what is this projectKey ?");
		jsap.registerParameter(opt);

		opt = new FlaggedOption("originalFilesPath");
		opt.setLongFlag("originalFilesPath");
		opt.setStringParser(JSAP.STRING_PARSER);
		opt.setDefault("./source/act/");
		opt.setHelp("The input folder or file for sonarqube-repair to work on");
		jsap.registerParameter(opt);

		opt = new FlaggedOption("prettyPrintingStrategy");
		opt.setLongFlag("prettyPrintingStrategy");
		opt.setStringParser(JSAP.STRING_PARSER);
		opt.setDefault(PrettyPrintingStrategy.NORMAL.name());
		opt.setHelp("Mode for pretty printing . NORMAL: default pretty print, SNIPER: sniper mode on for more precise code transformation pretty print");
		jsap.registerParameter(opt);

		opt = new FlaggedOption("fileOutputStrategy");
		opt.setLongFlag("fileOutputStrategy");
		opt.setStringParser(JSAP.STRING_PARSER);
		opt.setDefault(FileOutputStrategy.CHANGED_ONLY.name());
		opt.setHelp("Mode for output. CHANGED_ONLY: default choice outputing only files modified by processors, ALL: everything including those unchanged files");
		jsap.registerParameter(opt);

		opt = new FlaggedOption("workspace");
		opt.setLongFlag("workspace");
		opt.setStringParser(JSAP.STRING_PARSER);
		opt.setDefault("./sonar-workspace");
		opt.setHelp("Workspace of sonarqube-repair");
		jsap.registerParameter(opt);

		opt = new FlaggedOption("gitRepoPath");
		opt.setLongFlag("gitRepoPath");
		opt.setStringParser(JSAP.STRING_PARSER);
		opt.setHelp("Root Path of the input Github repo directory");
		jsap.registerParameter(opt);

		return jsap;
	}

	public void initConfig(JSAP jsap,String[] args) {
		JSAPResult jsapRes = jsap.parse(args);

		this.getConfig().addRuleKeys(jsapRes.getInt("ruleKeys"));
		this.getConfig().setProjectKey(jsapRes.getString("projectKey"));
		this.getConfig().setOriginalFilesPath(jsapRes.getString("originalFilesPath"));
		this.getConfig().setPrettyPrintingStrategy(PrettyPrintingStrategy.valueOf(jsapRes.getString("prettyPrintingStrategy")));
		this.getConfig().setFileOutputStrategy(FileOutputStrategy.valueOf(jsapRes.getString("fileOutputStrategy")));
		this.getConfig().setWorkspace(jsapRes.getString("workspace"));
		this.getConfig().setGitRepoPath(jsapRes.getString("gitRepoPath"));
	}


	public DefaultRepair getRepairProcess() {
		DefaultRepair defaultRepair = new DefaultRepair(this.config);
		return defaultRepair;
	}

	public void start(String[] args) throws Exception {
		JSAP jsap = this.defineArgs();
		this.initConfig(jsap,args);
		this.getRepairProcess().repair();
		System.out.println("done");
	}

	public static void main(String[] args) throws Exception{
		Main main = new Main();
		main.start(args);
	}
}
