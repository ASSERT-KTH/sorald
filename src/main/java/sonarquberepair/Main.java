package sonarquberepair;

import spoon.Launcher;
import spoon.processing.Processor;
import spoon.support.sniper.SniperJavaPrettyPrinter;

import java.lang.reflect.Constructor;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.stringparsers.EnumeratedStringParser;
import com.martiansoftware.jsap.stringparsers.FileStringParser;


public class Main {
	private final SonarQubeRepairConfig config = new SonarQubeRepairConfig();

	public SonarQubeRepairConfig getConfig() {
		return this.config;
	}

	public JSAP defineArgs() throws JSAPException{
		JSAP jsap = new JSAP();

		/* will be supporting multiple rules processing later so rulenumber(s) */
		FlaggedOption opt = new FlaggedOption("ruleNumbers");
		opt.setLongFlag("ruleNumbers");
		opt.setStringParser(JSAP.INTEGER_PARSER);
		opt.setDefault("2116");
		opt.setHelp("Sonarqube rule number, Check https://rules.sonarsource.com/java");
		jsap.registerParameter(opt);

		opt = new FlaggedOption("projectKey");
		opt.setLongFlag("projectKey");
		opt.setStringParser(JSAP.STRING_PARSER);
		opt.setDefault("fr.inria.gforge.spoon:spoon-core");
		opt.setHelp("what is this projectKey ? ");
		jsap.registerParameter(opt);

		opt = new FlaggedOption("repairPath");
		opt.setLongFlag("repairPath");
		opt.setStringParser(JSAP.STRING_PARSER);
		opt.setDefault("./source/act/");
		opt.setHelp("what is this projectKey ? ");
		jsap.registerParameter(opt);

		opt = new FlaggedOption("prettyPrintingStrategy");
		opt.setLongFlag("prettyPrintingStrategy");
		opt.setStringParser(JSAP.STRING_PARSER);
		opt.setDefault(PrettyPrintingStrategy.NORMAL.name());
		opt.setHelp("Mode for pretty printing . NORMAL: default pretty print, SNIPER: sniper mode on for more precise code transformation pretty print");
		jsap.registerParameter(opt);

		opt = new FlaggedOption("outputStrategy");
		opt.setLongFlag("outputStrategy");
		opt.setStringParser(JSAP.STRING_PARSER);
		opt.setDefault(OutputStrategy.ONLYCHANGED.name());
		opt.setHelp("Mode for output. ONLYCHANGED: default choice outputing only files modified by processors, ALL: everything including those unchanged files");
		jsap.registerParameter(opt);

		opt = new FlaggedOption("workspace");
		opt.setLongFlag("workspace");
		opt.setStringParser(JSAP.STRING_PARSER);
		opt.setDefault("./sonar-workspace");
		opt.setHelp("Workspace of SonarQubeRepair");
		jsap.registerParameter(opt);

		opt = new FlaggedOption("gitRepoPath");
		opt.setLongFlag("gitRepoPath");
		opt.setStringParser(JSAP.STRING_PARSER);
		opt.setHelp("Workspace of SonarQubeRepair");
		jsap.registerParameter(opt);

		return jsap;
	}

	public void initConfig(JSAP jsap,String[] args) {
		JSAPResult jsapRes = jsap.parse(args);

		this.getConfig().addRuleNumbers(jsapRes.getInt("ruleNumbers"));
		this.getConfig().setProjectKey(jsapRes.getString("projectKey"));
		this.getConfig().setRepairPath(jsapRes.getString("repairPath"));
		this.getConfig().setPrettyPrintingStrategy(PrettyPrintingStrategy.valueOf(jsapRes.getString("prettyPrintingStrategy")));
		this.getConfig().setOutputStrategy(OutputStrategy.valueOf(jsapRes.getString("outputStrategy")));
		this.getConfig().setWorkSpace(jsapRes.getString("workspace"));
		this.getConfig().setGitRepoPath(jsapRes.getString("gitRepoPath"));
	}


	public DefaultRepair getRepairProcess() {
		DefaultRepair repairProc = new DefaultRepair(this.config);
		return repairProc;
	}

	/**
	 * @param args string array. Give either 0, 1 or 2 arguments. first argument is sonarqube rule-number which you can get from https://rules.sonarsource.com/java/type/Bug
	 *             second argument is the projectKey for the sonarqube analysis of source files. for  example "fr.inria.gforge.spoon:spoon-core"
	 */
	public void start(String[] args) throws Exception {
		JSAP jsap = defineArgs();
		this.initConfig(jsap,args);
		this.getRepairProcess().repair();
		System.out.println("done");
	}

	public static void main(String[] args) throws Exception{
		Main main = new Main();
		main.start(args);
	}
}
