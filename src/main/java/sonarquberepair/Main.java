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
		opt.setHelp("Choose one of the following rule keys:" + Processors.getRuleDescriptions());
		jsap.registerParameter(opt);

		opt = new FlaggedOption("originalFilesPath");
		opt.setLongFlag("originalFilesPath");
		opt.setStringParser(FileStringParser.getParser().setMustExist(true));
		opt.setRequired(true);
		opt.setHelp("The path to the file or folder to be analyzed and possibly repaired.");
		jsap.registerParameter(opt);

		opt = new FlaggedOption("workspace");
		opt.setLongFlag("workspace");
		opt.setStringParser(JSAP.STRING_PARSER);
		opt.setDefault("./sonar-workspace");
		opt.setHelp("The path to a folder that will be used as workspace by sonarqube-repair, i.e. the path for the output.");
		jsap.registerParameter(opt);

		opt = new FlaggedOption("gitRepoPath");
		opt.setLongFlag("gitRepoPath");
		opt.setStringParser(FileStringParser.getParser().setMustExist(true).setMustBeDirectory(true));
		opt.setHelp("The path to a git repository directory.");
		jsap.registerParameter(opt);

		opt = new FlaggedOption("prettyPrintingStrategy");
		opt.setLongFlag("prettyPrintingStrategy");
		opt.setStringParser(JSAP.STRING_PARSER);
		opt.setDefault(PrettyPrintingStrategy.NORMAL.name());
		opt.setHelp("Mode for pretty printing the source code: 'NORMAL', which means that all source code will be printed and its formatting might change (such as indentation), and 'SNIPER', which means that only statements changed towards the repair of sonar rule violations will be printed.");
		jsap.registerParameter(opt);

		opt = new FlaggedOption("fileOutputStrategy");
		opt.setLongFlag("fileOutputStrategy");
		opt.setStringParser(JSAP.STRING_PARSER);
		opt.setDefault(FileOutputStrategy.CHANGED_ONLY.name());
		opt.setHelp("Mode for outputting files: 'CHANGED_ONLY', which means that only changed files will be created in the workspace, and 'ALL', which means that all files, including the unchanged ones, will be created in the workspace.");
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
		this.getConfig().setOriginalFilesPath(arguments.getFile("originalFilesPath").getAbsolutePath());
		this.getConfig().setWorkspace(arguments.getString("workspace"));
		if (arguments.getFile("gitRepoPath") != null) {
			this.getConfig().setGitRepoPath(arguments.getFile("gitRepoPath").getAbsolutePath());
		}
		this.getConfig().setPrettyPrintingStrategy(PrettyPrintingStrategy.valueOf(arguments.getString("prettyPrintingStrategy")));
		this.getConfig().setFileOutputStrategy(FileOutputStrategy.valueOf(arguments.getString("fileOutputStrategy")));
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
