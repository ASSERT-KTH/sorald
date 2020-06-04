package sorald;

import java.lang.reflect.Constructor;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import java.util.Map;
import spoon.Launcher;
import spoon.processing.Processor;
import spoon.reflect.declaration.CtType;
import spoon.support.sniper.SniperJavaPrettyPrinter;
import spoon.support.JavaOutputProcessor;
import spoon.support.QueueProcessingManager;
import spoon.processing.ProcessingManager;
import spoon.reflect.factory.Factory;

public class DefaultRepair {
	private final GitPatchGenerator generator = new GitPatchGenerator();
	private SoraldConfig config;
	private int patchedFileCounter = 0;

	public DefaultRepair(SoraldConfig config) {
		this.config = config;
		if (this.config.getGitRepoPath() != null) {
			this.generator.setGitProjectRootDir(this.config.getGitRepoPath());
		}
	}

	public void repair() {
		final String spoonedPath = this.config.getWorkspace() + File.separator + Constants.SPOONED;
		final String intermediateSpoonedPath = spoonedPath + File.separator + Constants.INTERMEDIATE;

		String inputDirPath;
		String outputDirPath;

		List<Integer> ruleKeys = this.config.getRuleKeys();
		for (int i = 0; i < ruleKeys.size(); i++) {
			int ruleKey = ruleKeys.get(i);

			if (ruleKeys.size() == 1) { // one processor, straightforward repair: we use the given input file dir, run one processor, directly output files (independently of the FileOutputStrategy) in the final output dir
				inputDirPath = this.config.getOriginalFilesPath();
				outputDirPath = spoonedPath;
			} else { // more than one processor, thus we need an intermediate dir, which will always contain all files (the changed and non-changed ones), because other processors will run on them
				if (i == 0) { // the first processor will run, thus we use the given input file dir and output *all* files in the intermediate dir
					inputDirPath = this.config.getOriginalFilesPath();
					outputDirPath = intermediateSpoonedPath;
				} else if ((i + 1) == ruleKeys.size()) { // the last processor will run, thus we use as input files the ones in the intermediate dir and output files in the final output dir
					inputDirPath = intermediateSpoonedPath;
					outputDirPath = spoonedPath;
				} else { // neither the first nor the last processor will run, thus use as input and output dirs the intermediate dir
					inputDirPath = outputDirPath = intermediateSpoonedPath;
				}
			}

			Launcher launcher = createLauncher(inputDirPath, outputDirPath);

			Processor processor = createProcessor(ruleKey, inputDirPath);

			Factory factory = launcher.getFactory();
			ProcessingManager processingManager = new QueueProcessingManager(factory);
			processingManager.addProcessor(processor);
			JavaOutputProcessor javaOutputProcessor = launcher.createOutputWriter();

			if (this.config.getFileOutputStrategy() == FileOutputStrategy.ALL
					|| outputDirPath.contains(intermediateSpoonedPath)) {
				processingManager.addProcessor(javaOutputProcessor);
			}
			processingManager.process(factory.Class().getAll());

			if (this.config.getFileOutputStrategy() == FileOutputStrategy.CHANGED_ONLY
					&& !outputDirPath.contains(intermediateSpoonedPath)) {
				for (Map.Entry<String, CtType> patchedFile : UniqueTypesCollector.getInstance().getTopLevelTypes4Output().entrySet()) {
					javaOutputProcessor.process(patchedFile.getValue());
					if (this.config.getGitRepoPath() != null) {
						createPatches(patchedFile.getKey(), javaOutputProcessor);
					}
				}
			}
		}

		deleteDirectory(new File(intermediateSpoonedPath));

		UniqueTypesCollector.getInstance().reset();
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

	private Launcher createLauncher(String inputDirPath, String outputDirPath) {
		Launcher launcher = new Launcher();

		launcher.addInputResource(inputDirPath);
		launcher.setSourceOutputDirectory(outputDirPath);
		launcher.getEnvironment().setAutoImports(true);
		launcher.getEnvironment().setIgnoreDuplicateDeclarations(true);
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
		launcher.buildModel();
		return launcher;
	}

	private Processor createProcessor(Integer ruleKey, String inputDirPath) {
		try {
			Class<?> processor = Processors.getProcessor(ruleKey);
			if (processor != null) {
				Constructor<?> cons = processor.getConstructor(String.class);
				Object object = cons.newInstance(inputDirPath);
				return (Processor) object;
			}
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
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