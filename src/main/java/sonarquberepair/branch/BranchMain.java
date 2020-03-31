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

import sonarquberepair.MainApi;

public class BranchMain implements MainApi{

	public static SonarQubeRepairConfig getConfig() {
		return SonarQubeRepairConfig.getInstance();
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

        opt = new FlaggedOption("repairMode");
        opt.setLongFlag("repairMode");
        opt.setStringParser(JSAP.STRING_PARSER);
        opt.setDefault(RepairMode.DEFAULT.name() + "");
        opt.setHelp("Mode for repair. DEFAULT: usual repair, SNIPER: sniper mode on for more precise code transformation");
        jsap.registerParameter(opt);

        opt = new FlaggedOption("workspace");
        opt.setLongFlag("workspace");
        opt.setStringParser(JSAP.STRING_PARSER);
        opt.setDefault("./sonar-workspace");
        opt.setHelp("Workspace of SonarQubeRepair");
        jsap.registerParameter(opt);

		return jsap;
	}

	public void initConfig(JSAP jsap,String[] args) {
		JSAPResult jsapRes = jsap.parse(args);

		this.getConfig().addRuleNumbers(jsapRes.getInt("ruleNumbers"));
		this.getConfig().setProjectKey(jsapRes.getString("projectKey"));
		this.getConfig().setRepairPath(jsapRes.getString("repairPath"));
		this.getConfig().setRepairMode(RepairMode.valueOf(jsapRes.getString("repairMode")));
		this.getConfig().setWorkSpace(jsapRes.getString("workspace"));
	}

	public IRepair getRepairProcess() {
		IRepair repairProc;
		if (this.getConfig().getRepairMode().name().equals("SNIPER")) {
			repairProc = new SniperRepair();
		} else {
			repairProc = new DefaultRepair();
		}
		return repairProc;
	}

	public void clean()	{

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

	@Override
	public void repair(String pathToFile, String projectKey, int ruleKey) throws Exception {};

	@Override
	public void normalRepair(String pathToFile, String projectKey, int ruleKey) throws Exception {};

}
