package sonarquberepair;

import spoon.Launcher;
import spoon.processing.Processor;
import spoon.support.sniper.SniperJavaPrettyPrinter;

import java.lang.reflect.Constructor;
import java.io.File;

import sonarquberepair.Processors;

public class DefaultRepair {
	private SonarQubeRepairConfig config;
	
	public DefaultRepair(SonarQubeRepairConfig config) {
		this.config = config;
	}

	public void repair() throws Exception {
		String outputDir = this.config.getWorkSpace() + File.separator + "spooned";

		Launcher launcher = new Launcher();
		launcher.addInputResource(this.config.getRepairPath());
		launcher.setSourceOutputDirectory(outputDir);
		launcher.getEnvironment().setAutoImports(true);
		if (this.config.getPrettyPrintingStrategy() == PrettyPrintingStrategy.SNIPER) {
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

		Class<?> processor = Processors.getProcessor(config.getRuleNumbers().get(0));
		Constructor<?> cons;
		Object object;
		try {
			cons = processor.getConstructor(String.class);
			object = cons.newInstance(config.getProjectKey());
		} catch (NoSuchMethodException e) {
			cons = processor.getConstructor();
			object = cons.newInstance();
		}
		launcher.addProcessor((Processor) object);
		launcher.run();
	}
}