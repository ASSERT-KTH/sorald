package sonarquberepair;

import spoon.Launcher;
import spoon.processing.Processor;
import spoon.support.sniper.SniperJavaPrettyPrinter;

import java.lang.reflect.Constructor;
import java.io.File;

import sonarquberepair.Processors;

public class DefaultRepair implements IRepair {

	@Override
	public void repair() throws Exception {
		String repairPath = SonarQubeRepairConfig.getInstance().getRepairPath();
		String projectKey = SonarQubeRepairConfig.getInstance().getProjectKey();
		int ruleKey = SonarQubeRepairConfig.getInstance().getRuleNumbers().get(0);
		PrettyPrintingStrategy prettyPrintingStrategy = SonarQubeRepairConfig.getInstance().getPrettyPrintingStrategy();

		Launcher launcher = new Launcher();
		launcher.addInputResource(repairPath);
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