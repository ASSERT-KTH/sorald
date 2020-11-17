package sorald;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import sorald.processor.SoraldAbstractProcessor;
import sorald.segment.FirstFitSegmentationAlgorithm;
import sorald.segment.Node;
import sorald.segment.SoraldTreeBuilderAlgorithm;
import spoon.Launcher;
import spoon.compiler.Environment;
import spoon.processing.ProcessingManager;
import spoon.processing.Processor;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.DefaultImportComparator;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;
import spoon.reflect.visitor.ImportCleaner;
import spoon.reflect.visitor.ImportConflictDetector;
import spoon.reflect.visitor.PrettyPrinter;
import spoon.support.JavaOutputProcessor;
import spoon.support.QueueProcessingManager;
import spoon.support.sniper.SniperJavaPrettyPrinter;

public class Repair {
    protected final GitPatchGenerator generator = new GitPatchGenerator();
    protected SoraldConfig config;
    private List<SoraldAbstractProcessor> addedProcessors = new ArrayList();
    protected int patchedFileCounter = 0;

    public Repair(SoraldConfig config) {
        this.config = config;
        if (this.config.getGitRepoPath() != null) {
            this.generator.setGitProjectRootDir(this.config.getGitRepoPath());
        }
    }

    public void repair() {
        List<Integer> ruleKeys = config.getRuleKeys();

        final String spoonedPath = this.config.getWorkspace() + File.separator + Constants.SPOONED;
        final String intermediateSpoonedPath =
                spoonedPath + File.separator + Constants.INTERMEDIATE;

        String inputDirPath;
        String outputDirPath;

        for (int i = 0; i < ruleKeys.size(); i++) {
            int ruleKey = ruleKeys.get(i);

            if (ruleKeys.size()
                    == 1) { // one processor, straightforward repair: we use the given input file
                // dir, run one
                // processor, directly output files (independently of the FileOutputStrategy) in
                // the final output dir
                inputDirPath = this.config.getOriginalFilesPath();
                outputDirPath = spoonedPath;
            } else { // more than one processor, thus we need an intermediate dir, which will always
                // contain all files (the changed and non-changed ones), because other processors
                // will run on them
                if (i == 0) { // the first processor will run, thus we use the given input file
                    // dir and output
                    // *all* files in the intermediate dir
                    inputDirPath = this.config.getOriginalFilesPath();
                    outputDirPath = intermediateSpoonedPath;
                } else if ((i + 1)
                        == ruleKeys.size()) { // the last processor will run, thus we use as input
                    // files the ones in
                    // the intermediate dir and output files in the final output dir
                    inputDirPath = intermediateSpoonedPath;
                    outputDirPath = spoonedPath;
                } else { // neither the first nor the last processor will run, thus use as input and
                    // output
                    // dirs the intermediate dir
                    inputDirPath = outputDirPath = intermediateSpoonedPath;
                }
            }

            SoraldAbstractProcessor<?> processor = createProcessor(ruleKey);
            if (config.getRepairStrategy() == RepairStrategy.DEFAULT) {
                CtModel model = defaultRepair(inputDirPath, outputDirPath, processor);

                JavaOutputProcessor javaOutputProcessor = new JavaOutputProcessor();
                javaOutputProcessor.setFactory(model.getUnnamedModule().getFactory());
                QueueProcessingManager processingManager =
                        new QueueProcessingManager(model.getUnnamedModule().getFactory());
                processingManager.addProcessor(javaOutputProcessor);

                if (this.config.getFileOutputStrategy() == FileOutputStrategy.ALL
                        || outputDirPath.contains(intermediateSpoonedPath)) {
                    processingManager.process(model.getAllTypes());
                }

                if (this.config.getFileOutputStrategy() == FileOutputStrategy.CHANGED_ONLY
                        && !outputDirPath.contains(intermediateSpoonedPath)) {
                    for (Map.Entry<String, CtType> patchedFile :
                            UniqueTypesCollector.getInstance()
                                    .getTopLevelTypes4Output()
                                    .entrySet()) {
                        javaOutputProcessor.process(patchedFile.getValue());
                        if (this.config.getGitRepoPath() != null) {
                            createPatches(patchedFile.getKey(), javaOutputProcessor);
                        }
                    }
                }
            } else {
                assert config.getRepairStrategy() == RepairStrategy.SEGMENT;
                segmentRepair(inputDirPath, outputDirPath, processor);
            }
        }

        printEndProcess();
        FileUtils.deleteDirectory(new File(intermediateSpoonedPath));
        UniqueTypesCollector.getInstance().reset();
    }

    public CtModel defaultRepair(
            String inputDirPath, String outputDirPath, SoraldAbstractProcessor<?> processor) {
        File inputBaseDir = FileUtils.getClosestDirectory(new File(inputDirPath));
        processor.initResource(inputDirPath, inputBaseDir);

        Launcher launcher = new Launcher();
        launcher.addInputResource(inputDirPath);
        initLauncher(launcher, outputDirPath);

        Factory factory = launcher.getFactory();
        ProcessingManager processingManager = new QueueProcessingManager(factory);
        processingManager.addProcessor(processor);
        processingManager.process(factory.Class().getAll());
        return launcher.getModel();
    }

    public void segmentRepair(
            String inputDirPath, String outputDirPath, SoraldAbstractProcessor<?> processor) {
        Node rootNode = SoraldTreeBuilderAlgorithm.buildTree(inputDirPath);
        LinkedList<LinkedList<Node>> segments =
                FirstFitSegmentationAlgorithm.segment(rootNode, config.getMaxFilesPerSegment());

        int nbFixes = 0;
        while (!segments.isEmpty() && nbFixes != this.config.getMaxFixesPerRule()) {
            List<Node> segment = segments.pop();
            File inputBaseDir = FileUtils.getClosestDirectory(new File(inputDirPath));
            processor.initResource(segment, inputBaseDir);
            try {
                Launcher launcher = createSegmentLauncher(segment, outputDirPath);

                Factory factory = launcher.getFactory();
                ProcessingManager processingManager = new QueueProcessingManager(factory);
                processingManager.addProcessor(processor);
                JavaOutputProcessor javaOutputProcessor = launcher.createOutputWriter();
                processingManager.process(factory.Class().getAll());

                if (this.config.getFileOutputStrategy() == FileOutputStrategy.CHANGED_ONLY) {
                    for (Map.Entry<String, CtType> patchedFile :
                            UniqueTypesCollector.getInstance()
                                    .getTopLevelTypes4Output()
                                    .entrySet()) {
                        javaOutputProcessor.process(patchedFile.getValue());
                        if (this.config.getGitRepoPath() != null) {
                            createPatches(patchedFile.getKey(), javaOutputProcessor);
                        }
                    }
                }
                nbFixes = processor.getNbFixes();
            } catch (Exception e) {
                System.out.println("Error while repairing, will ignore this segment");
            }
        }
        this.printEndProcess();
        UniqueTypesCollector.getInstance().reset();
    }

    private Launcher createSegmentLauncher(List<Node> segment, String outputDirPath) {
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

    public void printEndProcess() {
        System.out.println("-----Number of fixes------");
        for (SoraldAbstractProcessor processor : addedProcessors) {
            System.out.println(
                    processor.getClass().getSimpleName() + ": " + processor.getNbFixes());
        }
        System.out.println("-----End of report------");
    }

    protected void createPatches(String patchedFilePath, JavaOutputProcessor javaOutputProcessor) {
        File patchDir = new File(this.config.getWorkspace() + File.separator + Constants.PATCHES);

        if (!patchDir.exists()) {
            patchDir.mkdirs();
        }
        List<File> list = javaOutputProcessor.getCreatedFiles();
        if (!list.isEmpty()) {
            String outputPath = list.get(list.size() - 1).getAbsolutePath();
            generator.generate(
                    patchedFilePath,
                    outputPath,
                    patchDir.getAbsolutePath()
                            + File.separator
                            + Constants.PATCH_FILE_PREFIX
                            + this.patchedFileCounter);
            this.patchedFileCounter++;
        }
    }

    protected Launcher initLauncher(Launcher launcher, String outputDirPath) {
        launcher.setSourceOutputDirectory(outputDirPath);
        Environment env = launcher.getEnvironment();
        env.setIgnoreDuplicateDeclarations(true);

        // this is a workaround for https://github.com/INRIA/spoon/issues/3693
        if (config.getPrettyPrintingStrategy() == PrettyPrintingStrategy.SNIPER) {
            env.setPrettyPrinterCreator(() -> new SniperJavaPrettyPrinter(env));
        }

        // need to build the model before setting the pretty-printer as the preprocessors need
        // data from the model
        CtModel model = launcher.buildModel();

        setPrettyPrinter(env, model);
        return launcher;
    }

    private void setPrettyPrinter(Environment env, CtModel model) {
        Supplier<? extends DefaultJavaPrettyPrinter> basePrinterCreator =
                config.getPrettyPrintingStrategy() == PrettyPrintingStrategy.SNIPER
                        ? createSniperPrinter(env)
                        : createDefaultPrinter(env);
        Supplier<PrettyPrinter> configuredPrinterCreator =
                applyCommonPrinterOptions(basePrinterCreator, model);
        env.setPrettyPrinterCreator(configuredPrinterCreator);
    }

    private static Supplier<PrettyPrinter> applyCommonPrinterOptions(
            Supplier<? extends DefaultJavaPrettyPrinter> prettyPrinterCreator, CtModel model) {
        Collection<CtTypeReference<?>> existingReferences = model.getElements(e -> true);
        List<Processor<CtElement>> preprocessors =
                List.of(
                        new SelectiveForceImport(existingReferences),
                        new ImportConflictDetector(),
                        new ImportCleaner().setImportComparator(new DefaultImportComparator()));
        return () -> {
            DefaultJavaPrettyPrinter printer = prettyPrinterCreator.get();
            printer.setIgnoreImplicit(false);
            printer.setPreprocessors(preprocessors);
            return printer;
        };
    }

    private static Supplier<? extends DefaultJavaPrettyPrinter> createSniperPrinter(
            Environment env) {
        env.setCommentEnabled(true);
        env.useTabulations(true);
        env.setTabulationSize(4);
        return () -> new SniperJavaPrettyPrinter(env);
    }

    private static Supplier<? extends DefaultJavaPrettyPrinter> createDefaultPrinter(
            Environment env) {
        return () -> new DefaultJavaPrettyPrinter(env);
    }

    protected SoraldAbstractProcessor createBaseProcessor(Integer ruleKey) {
        try {
            Class<?> processor = Processors.getProcessor(ruleKey);
            if (processor != null) {
                Constructor<?> cons = processor.getConstructor();
                SoraldAbstractProcessor object = (SoraldAbstractProcessor) cons.newInstance();
                return object;
            }
        } catch (InstantiationException
                | IllegalAccessException
                | InvocationTargetException
                | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    private SoraldAbstractProcessor<?> createProcessor(Integer ruleKey) {
        SoraldAbstractProcessor<?> processor = createBaseProcessor(ruleKey);
        if (processor != null) {
            return processor.setMaxFixes(config.getMaxFixesPerRule());
        }
        return null;
    }
}
