package sorald;

import sorald.processor.SoraldAbstractProcessor;

import java.lang.reflect.Constructor;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
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

public class SegmentedRepair implements IRepair {
	private final GitPatchGenerator generator = new GitPatchGenerator();
	private SoraldConfig config;
	private int patchedFileCounter = 0;
	private HashMap<String,Integer> processorNbsRepaired = new HashMap<String,Integer>();
	public SegmentedRepair(SoraldConfig config) {
		this.config = config;
		if (this.config.getGitRepoPath() != null) {
			this.generator.setGitProjectRootDir(this.config.getGitRepoPath());
		}
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

				Processor processor = createProcessor(ruleKey, segment, nbFixes);
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

	// FIXME: this method was copied from TestHelper.java. We should extract it to a FileHelper to be visible for both main code and test code.
	public static boolean deleteDirectory(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.delete();
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
	
		launcher.setSourceOutputDirectory(outputDirPath);
		launcher.getEnvironment().setAutoImports(true);
		launcher.getEnvironment().setIgnoreDuplicateDeclarations(true);
		if (this.config.getPrettyPrintingStrategy() == PrettyPrintingStrategy.SNIPER) {
			launcher.getEnvironment().setPrettyPrinterCreator(() -> {
						SniperJavaPrettyPrinter sniper = new SniperJavaPrettyPrinter(launcher.getEnvironment());
						sniper.setIgnoreImplicit(true);
						return sniper;
					}
			);
			launcher.getEnvironment().setCommentEnabled(true);
			launcher.getEnvironment().useTabulations(true);
			launcher.getEnvironment().setTabulationSize(4);
		}
		launcher.buildModel();
		return launcher;
	}

	private Processor createProcessor(Integer ruleKey, List<Node> segment, int cachedNbFixes) throws Exception {
		Class<?> processor = Processors.getProcessor(ruleKey);
		if (processor != null) {
			Constructor<?> cons = processor.getConstructor(List.class);
			SoraldAbstractProcessor object = ((SoraldAbstractProcessor)cons.newInstance(segment)).setMaxFixes(this.config.getMaxFixesPerRule()).setNbFixes(cachedNbFixes);
			if (!this.processorNbsRepaired.containsKey(object.getClass().getSimpleName())) {
				this.processorNbsRepaired.put(object.getClass().getSimpleName(),cachedNbFixes);
			}
			return object;
		}
		return null;
	}

	private void createPatches(String patchedFilePath, JavaOutputProcessor javaOutputProcessor) {
		File patchDir = new File(this.config.getWorkspace() + File.separator + Constants.PATCHES);

		if (!patchDir.exists()) {
			patchDir.mkdirs();
		}
		List<File> list = javaOutputProcessor.getCreatedFiles();
		if (!list.isEmpty()) {
			String outputPath = list.get(list.size() - 1).getAbsolutePath();
			generator.generate(patchedFilePath,outputPath, patchDir.getAbsolutePath() + File.separator + Constants.PATCH_FILE_PREFIX + this.patchedFileCounter);
			this.patchedFileCounter++;
		}
	}
}