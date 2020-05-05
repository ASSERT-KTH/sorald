package sonarquberepair;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.stringparsers.FileStringParser;


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

		opt = new FlaggedOption("originalFilesPath");
		opt.setLongFlag("originalFilesPath");
		opt.setStringParser(FileStringParser.getParser().setMustExist(true));
		opt.setRequired(true);
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
		opt.setStringParser(FileStringParser.getParser().setMustExist(true).setMustBeDirectory(true));
		opt.setHelp("Root Path of the input Github repo directory");
		jsap.registerParameter(opt);

		Switch sw = new Switch("help");
		sw.setShortFlag('h');
		sw.setLongFlag("help");
		sw.setDefault("false");
		jsap.registerParameter(sw);

		return jsap;
	}

	public void checkArguments(JSAP jsap, JSAPResult arguments) {
		if (!arguments.success()) {
			for (java.util.Iterator<?> errors = arguments.getErrorMessageIterator(); errors.hasNext();) {
				System.err.println("Error: " + errors.next());
			}
			printUsage(jsap);
		}
		if (arguments.getBoolean("help")) {
			printUsage(jsap);
		}
	}

	public static void printUsage(JSAP jsap) {
		System.err.println("Arguments: ");
		System.err.println();
		System.err.println(jsap.getHelp());
		System.exit(-1);
	}

	public void initConfig(JSAPResult arguments) {
		this.getConfig().addRuleKeys(arguments.getInt("ruleKeys"));
		this.getConfig().setOriginalFilesPath(arguments.getString("originalFilesPath"));
		this.getConfig().setPrettyPrintingStrategy(PrettyPrintingStrategy.valueOf(arguments.getString("prettyPrintingStrategy")));
		this.getConfig().setFileOutputStrategy(FileOutputStrategy.valueOf(arguments.getString("fileOutputStrategy")));
		this.getConfig().setWorkspace(arguments.getString("workspace"));
		this.getConfig().setGitRepoPath(arguments.getString("gitRepoPath"));
	}


	public DefaultRepair getRepairProcess() {
		DefaultRepair defaultRepair = new DefaultRepair(this.config);
		return defaultRepair;
	}

	public void start(String[] args) throws Exception {
		JSAP jsap = this.defineArgs();
		JSAPResult arguments = jsap.parse(args);
		this.checkArguments(jsap, arguments);
		this.initConfig(arguments);
		this.getRepairProcess().repair();
		System.out.println("done");
	}

	public static void main(String[] args) throws Exception{
		Main main = new Main();
		main.start(args);
	}
}
