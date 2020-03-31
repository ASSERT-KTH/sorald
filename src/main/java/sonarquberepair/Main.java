package sonarquberepair;

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.FlaggedOption;

import sonarquberepair.branch.BranchMain;

import java.util.Arrays;

public class Main {
	private static MainApi main;

	public static void repair(String pathToFile, String projectKey, int ruleKey, PrettyPrintingStrategy prettyPrintingStrategy) throws Exception {
		Launcher launcher = new Launcher();
		launcher.addInputResource(pathToFile);
		launcher.getEnvironment().setAutoImports(true);
		if (prettyPrintingStrategy == PrettyPrintingStrategy.SNIPER) {
			launcher.getEnvironment().setPrettyPrinterCreator(() -> {
						SniperJavaPrettyPrinter sniper = new SniperJavaPrettyPrinter(launcher.getEnvironment());
						sniper.setIgnoreImplicit(false);
						return sniper;
					}
			);
			launcher.getEnvironment().setCommentEnabled(true);
			launcher.getEnvironment().useTabulations(true);
			launcher.getEnvironment().setTabulationSize(4);
		}

		Class<?> processor = Processors.getProcessor(ruleKey);
		Constructor<?> cons;
		Object object;
		try {
			cons = processor.getConstructor(String.class);
			object = cons.newInstance(projectKey);
		} catch (NoSuchMethodException e) {
			cons = processor.getConstructor();
			object = cons.newInstance();
		}
		launcher.addProcessor((Processor) object);
		launcher.run();
	}

	/**
	 * @param args string array.
	 *             Give one or two arguments.
	 *             The first argument is the Sonar rule key.
	 *             The second argument is the project key for the Sonar analysis of source files.
	 */
	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.out.println("Please, provide the Sonar rule key, and optionally provide also the project key for the Sonar analysis.");
			return;
		}
		if (args.length > 2) {
			throw new IllegalArgumentException("Provide one or two arguments (only the Sonar rule key, and optionally also the project key for the Sonar analysis).");
		}

		int ruleKey = Integer.parseInt(args[0]);
		String projectKey = "";
		if (args.length == 2) {
			projectKey = args[1];
		}
		System.out.println("Applying " + Processors.getProcessor(ruleKey).getName() + "...");
		repair("./source/act/", projectKey, ruleKey, PrettyPrintingStrategy.SNIPER);
		System.out.println("Done.");
	}

	public static void main(String[] args) throws Exception{
		System.out.println(Arrays.toString(args));
		main = getMain(args);
		main.start(args);
	}
}