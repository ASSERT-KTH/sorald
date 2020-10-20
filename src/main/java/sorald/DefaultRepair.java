package sorald;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import sorald.processor.SoraldAbstractProcessor;
import spoon.Launcher;
import spoon.processing.ProcessingManager;
import spoon.processing.Processor;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.support.JavaOutputProcessor;
import spoon.support.QueueProcessingManager;

public class DefaultRepair extends SoraldAbstractRepair {
  private List<SoraldAbstractProcessor> addedProcessors = new ArrayList();

  public DefaultRepair(SoraldConfig config) {
    super(config);
  }

  public void repair() {
    final String spoonedPath = this.config.getWorkspace() + File.separator + Constants.SPOONED;
    final String intermediateSpoonedPath = spoonedPath + File.separator + Constants.INTERMEDIATE;

    String inputDirPath;
    String outputDirPath;

    List<Integer> ruleKeys = this.config.getRuleKeys();
    for (int i = 0; i < ruleKeys.size(); i++) {
      int ruleKey = ruleKeys.get(i);

      if (ruleKeys.size()
          == 1) { // one processor, straightforward repair: we use the given input file dir, run one
        // processor, directly output files (independently of the FileOutputStrategy) in
        // the final output dir
        inputDirPath = this.config.getOriginalFilesPath();
        outputDirPath = spoonedPath;
      } else { // more than one processor, thus we need an intermediate dir, which will always
        // contain all files (the changed and non-changed ones), because other processors
        // will run on them
        if (i
            == 0) { // the first processor will run, thus we use the given input file dir and output
          // *all* files in the intermediate dir
          inputDirPath = this.config.getOriginalFilesPath();
          outputDirPath = intermediateSpoonedPath;
        } else if ((i + 1)
            == ruleKeys
                .size()) { // the last processor will run, thus we use as input files the ones in
          // the intermediate dir and output files in the final output dir
          inputDirPath = intermediateSpoonedPath;
          outputDirPath = spoonedPath;
        } else { // neither the first nor the last processor will run, thus use as input and output
          // dirs the intermediate dir
          inputDirPath = outputDirPath = intermediateSpoonedPath;
        }
      }

      Launcher launcher = createLauncher(inputDirPath, outputDirPath);

      Processor processor = createProcessor(ruleKey, inputDirPath);
      this.addedProcessors.add((SoraldAbstractProcessor) processor);
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
        for (Map.Entry<String, CtType> patchedFile :
            UniqueTypesCollector.getInstance().getTopLevelTypes4Output().entrySet()) {
          javaOutputProcessor.process(patchedFile.getValue());
          if (this.config.getGitRepoPath() != null) {
            createPatches(patchedFile.getKey(), javaOutputProcessor);
          }
        }
      }
    }

    this.printEndProcess();
    deleteDirectory(new File(intermediateSpoonedPath));
    UniqueTypesCollector.getInstance().reset();
  }

  public void printEndProcess() {
    System.out.println("-----Number of fixes------");
    for (SoraldAbstractProcessor processor : addedProcessors) {
      System.out.println(processor.getClass().getSimpleName() + ": " + processor.getNbFixes());
    }
    System.out.println("-----End of report------");
  }

  private Launcher createLauncher(String inputDirPath, String outputDirPath) {
    Launcher launcher = new Launcher();

    launcher.addInputResource(inputDirPath);
    return initLauncher(launcher, outputDirPath);
  }

  private Processor createProcessor(Integer ruleKey, String inputDirPath) {
    SoraldAbstractProcessor processor = createBaseProcessor(ruleKey);
    if (processor != null) {
      return processor.initResource(inputDirPath).setMaxFixes(this.config.getMaxFixesPerRule());
    }
    return null;
  }
}
