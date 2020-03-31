package sonarquberepair;

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.FlaggedOption;

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

	public static MainApi getMain(String[] args) throws JSAPException{
		JSAP jsap = new JSAP();

		/* will be supporting multiple rules processing later so rulenumber(s) */
		FlaggedOption opt = new FlaggedOption("versionMode");
        opt.setLongFlag("versionMode");
        opt.setStringParser(JSAP.STRING_PARSER);
        opt.setDefault("LNameEGACY");
        opt.setHelp("LEGACY: use legacy sonarqube repair. NEW: current sonarqube repair.");
        jsap.registerParameter(opt);

        JSAPResult res = jsap.parse(args);
        String mode = res.getString("versionMode");
        if (mode.equals("NEW")) {
		return new BranchMain();
        } else {
		return new LegacyMain();
        }
	}

	public static void main(String[] args) throws Exception{
		main = getMain(args);
		main.start(args);
		SonarQubeRepairConfig.resetConfig(); // in case, when we call main as a method instead of entry point (for test cases)
	}
}