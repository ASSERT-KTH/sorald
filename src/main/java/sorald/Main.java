package sorald;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.stringparsers.FileStringParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;

public class Main {
	private final SoraldConfig config = new SoraldConfig();

	public SoraldConfig getConfig() {
		return this.config;
	}

	public JSAP defineArgs() throws JSAPException{
		JSAP jsap = new JSAP();

		FlaggedOption opt = new FlaggedOption(Constants.ARG_RULE_KEYS);
		opt.setLongFlag(Constants.ARG_RULE_KEYS);
		opt.setStringParser(JSAP.STRING_PARSER);
		opt.setList(true);
		opt.setListSeparator(',');
		opt.setHelp("Choose one or more of the following rule keys (use ',' to separate multiple keys):" + Processors.getRuleDescriptions());
		jsap.registerParameter(opt);

		opt = new FlaggedOption(Constants.ARG_ORIGINAL_FILES_PATH);
		opt.setLongFlag(Constants.ARG_ORIGINAL_FILES_PATH);
		opt.setStringParser(FileStringParser.getParser().setMustExist(true));
		opt.setRequired(true);
		opt.setHelp("The path to the file or folder to be analyzed and possibly repaired.");
		jsap.registerParameter(opt);

		opt = new FlaggedOption(Constants.ARG_WORKSPACE);
		opt.setLongFlag(Constants.ARG_WORKSPACE);
		opt.setStringParser(JSAP.STRING_PARSER);
		opt.setDefault("./" + Constants.SORALD_WORKSPACE);
		opt.setHelp("The path to a folder that will be used as workspace by Sorald, i.e. the path for the output.");
		jsap.registerParameter(opt);

		opt = new FlaggedOption(Constants.ARG_GIT_REPO_PATH);
		opt.setLongFlag(Constants.ARG_GIT_REPO_PATH);
		opt.setStringParser(FileStringParser.getParser().setMustExist(true).setMustBeDirectory(true));
		opt.setHelp("The path to a git repository directory.");
		jsap.registerParameter(opt);

		opt = new FlaggedOption(Constants.ARG_PRETTY_PRINTING_STRATEGY);
		opt.setLongFlag(Constants.ARG_PRETTY_PRINTING_STRATEGY);
		opt.setStringParser(JSAP.STRING_PARSER);
		opt.setDefault(PrettyPrintingStrategy.NORMAL.name());
		opt.setHelp("Mode for pretty printing the source code: 'NORMAL', which means that all source code will be printed and its formatting might change (such as indentation), and 'SNIPER', which means that only statements changed towards the repair of Sonar rule violations will be printed.");
		jsap.registerParameter(opt);

		opt = new FlaggedOption(Constants.ARG_FILE_OUTPUT_STRATEGY);
		opt.setLongFlag(Constants.ARG_FILE_OUTPUT_STRATEGY);
		opt.setStringParser(JSAP.STRING_PARSER);
		opt.setDefault(FileOutputStrategy.CHANGED_ONLY.name());
		opt.setHelp("Mode for outputting files: 'CHANGED_ONLY', which means that only changed files will be created in the workspace, and 'ALL', which means that all files, including the unchanged ones, will be created in the workspace.");
		jsap.registerParameter(opt);

		opt = new FlaggedOption(Constants.ARG_MAX_FIXES_PER_RULE);
		opt.setLongFlag(Constants.ARG_MAX_FIXES_PER_RULE);
		opt.setStringParser(JSAP.INTEGER_PARSER);
		opt.setDefault("" + Integer.MAX_VALUE);
		opt.setHelp("Max number of fixes per rule. Default: Integer.MAX_VALUE (or all)");
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

		for (String ruleKey : arguments.getStringArray(Constants.ARG_RULE_KEYS)) {
			if (Processors.getProcessor(Integer.parseInt(ruleKey)) == null) {
				System.out.println("Sorry, repair not available for rule " + ruleKey +
						". See the available rules below.");
				printUsage(jsap);
			}
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
		List<Integer> ruleKeys = new ArrayList<>(Arrays.asList(arguments.getStringArray(Constants.ARG_RULE_KEYS)).stream()
				.map(s -> Integer.parseInt(s))
				.collect(Collectors.toList()));
		this.getConfig().addRuleKeys(ruleKeys);
		this.getConfig().setOriginalFilesPath(arguments.getFile(Constants.ARG_ORIGINAL_FILES_PATH).getAbsolutePath());
		this.getConfig().setWorkspace(arguments.getString(Constants.ARG_WORKSPACE));
		if (arguments.getFile(Constants.ARG_GIT_REPO_PATH) != null) {
			this.getConfig().setGitRepoPath(arguments.getFile(Constants.ARG_GIT_REPO_PATH).getAbsolutePath());
		}
		this.getConfig().setPrettyPrintingStrategy(PrettyPrintingStrategy.valueOf(arguments.getString(Constants.ARG_PRETTY_PRINTING_STRATEGY)));
		this.getConfig().setFileOutputStrategy(FileOutputStrategy.valueOf(arguments.getString(Constants.ARG_FILE_OUTPUT_STRATEGY)));
		this.getConfig().setMaxFixesPerRule(arguments.getInt(Constants.ARG_MAX_FIXES_PER_RULE));
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
