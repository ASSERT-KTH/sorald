package sonarquberepair;

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.FlaggedOption;

import java.util.Arrays;

public class Main {
	private static MainApi main;


	/* abstraction from legacty main */
	public static void repair(String pathToFile, String projectKey, int ruleKey, PrettyPrintingStrategy prettyPrintingStrategy) throws Exception {
		main = new LegacyMain();
		main.repair(pathToFile,projectKey,ruleKey,prettyPrintingStrategy);
	}

	public static MainApi getMain(String[] args) throws JSAPException{
		JSAP jsap = new JSAP();

		/* will be supporting multiple rules processing later so rulenumber(s) */
		FlaggedOption opt = new FlaggedOption("versionMode");
        opt.setLongFlag("versionMode");
        opt.setStringParser(JSAP.STRING_PARSER);
        opt.setDefault("LEGACY");
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
	}
}