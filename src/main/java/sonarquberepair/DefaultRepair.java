package sonarquberepair;

import spoon.Launcher;
import spoon.processing.Processor;
import spoon.support.sniper.SniperJavaPrettyPrinter;

import java.lang.reflect.Constructor;
import java.io.File;

import sonarquberepair.Processors;

public class DefaultRepair {
	private String repairPath;
	private String projectKey;
	private String workspace;
	private int ruleKey;
	private PrettyPrintingStrategy prettyPrintingStrategy;

	public DefaultRepair(String repairPath, String projectKey, String workspace,int ruleKey,PrettyPrintingStrategy prettyPrintingStrategy) {
		this.repairPath = repairPath;
		this.projectKey = projectKey;
		this.workspace = workspace;
		this.ruleKey = ruleKey;
		this.prettyPrintingStrategy = prettyPrintingStrategy;
	}
	
	public DefaultRepair(SonarQubeRepairConfig config) {
		this(config.getRepairPath(),config.getProjectKey(),config.getWorkSpace(),config.getRuleNumbers().get(0),config.getPrettyPrintingStrategy());
	}

	public void repair() throws Exception {
		String repairPath = this.repairPath;
		String projectKey = this.projectKey;
		String outputDir = this.workspace + File.separator + "spooned";
		int ruleKey = this.ruleKey;
		PrettyPrintingStrategy prettyPrintingStrategy = this.prettyPrintingStrategy;

		Launcher launcher = new Launcher();
		launcher.addInputResource(repairPath);
		launcher.setSourceOutputDirectory(outputDir);
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
}