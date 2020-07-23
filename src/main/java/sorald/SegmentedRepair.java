package sorald;

import sorald.processor.SoraldAbstractProcessor;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import spoon.Launcher;
import spoon.processing.Processor;
import spoon.reflect.declaration.CtType;
import spoon.support.sniper.SniperJavaPrettyPrinter;
import spoon.support.JavaOutputProcessor;
import spoon.support.QueueProcessingManager;
import spoon.processing.ProcessingManager;
import spoon.reflect.factory.Factory;

import java.util.LinkedList;
import sorald.FileTreeAlgorithm.Node;
import sorald.SoraldConfig;

import java.util.HashMap;

public class SegmentedRepair extends SoraldAbstractRepair {
	private HashMap<String,Integer> processorNbsRepaired = new HashMap<String,Integer>();
	
	public SegmentedRepair(SoraldConfig config) {
		super(config);
	}

	public void repair() {
		LinkedList<LinkedList<Node>> segments = this.config.getSegments();
		final String outputDirPath = this.config.getWorkspace() + File.separator + Constants.SPOONED;

		List<Integer> ruleKeys = this.config.getRuleKeys();
		int ruleKey = ruleKeys.get(0);
		int nbFixes = 0;
		while (!segments.isEmpty() && nbFixes != this.config.getMaxFixesPerRule()) {
			List<Node> segment = segments.pop();
			try {
				Launcher launcher = createLauncher(segment, outputDirPath);

				SoraldAbstractProcessor processor = createProcessor(ruleKey, segment, nbFixes);
				if (!this.processorNbsRepaired.containsKey(processor.getClass().getSimpleName())) {
					this.processorNbsRepaired.put(processor.getClass().getSimpleName(),nbFixes);
				}
				Factory factory = launcher.getFactory();
				ProcessingManager processingManager = new QueueProcessingManager(factory);
				processingManager.addProcessor(processor);
				JavaOutputProcessor javaOutputProcessor = launcher.createOutputWriter();
				processingManager.process(factory.Class().getAll());

				if (this.config.getFileOutputStrategy() == FileOutputStrategy.CHANGED_ONLY) {
					for (Map.Entry<String, CtType> patchedFile : UniqueTypesCollector.getInstance().getTopLevelTypes4Output().entrySet()) {
						javaOutputProcessor.process(patchedFile.getValue());
						if (this.config.getGitRepoPath() != null) {
							createPatches(patchedFile.getKey(), javaOutputProcessor);
						}
					}
				}
				nbFixes = ((SoraldAbstractProcessor) processor).getNbFixes();
				this.processorNbsRepaired.put(processor.getClass().getSimpleName(),nbFixes);
			} catch (Exception e) {
				System.out.println("Error while repairing, will ignore this segment");
			}
		}
		this.printEndProcess();
		UniqueTypesCollector.getInstance().reset();
	}

	public void printEndProcess() {
		System.out.println("-----Number of fixes------");
		for (String processor : processorNbsRepaired.keySet()) {
			System.out.println(processor + ": " + processorNbsRepaired.get(processor));
		}
		System.out.println("-----End of report------");
	}

	private Launcher createLauncher(List<Node> segment,String outputDirPath) {
		Launcher launcher = new Launcher();

		for (Node node : segment) {
			if (node.isDirNode()) {
				launcher.addInputResource(node.getRootPath());
			} else {
				for (String file : node.getJavaFiles()) {
					launcher.addInputResource(file);
				}
			}
		}
		return initLauncher(launcher, outputDirPath);
	}

	private SoraldAbstractProcessor createProcessor(Integer ruleKey, List<Node> segment, int cachedNbFixes) throws Exception {
		SoraldAbstractProcessor processor = createBaseProcessor(ruleKey);
		if (processor != null) {
			return processor.initResource(segment)
							.setMaxFixes(this.config.getMaxFixesPerRule())
							.setNbFixes(cachedNbFixes);
		}
		return null;
	}
}