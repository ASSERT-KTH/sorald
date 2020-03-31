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

		System.out.println(repairPath + " " + projectKey + " " + ruleKey);
		//Not Sniper  Mode
		Launcher launcher = new Launcher();
		launcher.addInputResource(repairPath);
		launcher.setSourceOutputDirectory(SonarQubeRepairConfig.getInstance().getWorkSpace() + File.separator + "spooned");
		launcher.getEnvironment().setAutoImports(true);
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
//        new SpoonModelTree(launcher.getFactory());
	}
}