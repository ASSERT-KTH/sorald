package sorald;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import sorald.event.EventHelper;
import sorald.event.EventType;
import sorald.event.SoraldEventHandler;
import sorald.processor.SoraldAbstractProcessor;
import sorald.segment.FirstFitSegmentationAlgorithm;
import sorald.segment.Node;
import sorald.segment.SoraldTreeBuilderAlgorithm;
import sorald.sonar.Checks;
import sorald.sonar.ProjectScanner;
import sorald.sonar.RuleViolation;
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
import spoon.support.DefaultOutputDestinationHandler;
import spoon.support.JavaOutputProcessor;
import spoon.support.QueueProcessingManager;
import spoon.support.sniper.SniperJavaPrettyPrinter;

/** Class for repairing projects. */
public class Repair {
    private final GitPatchGenerator generator = new GitPatchGenerator();
    private final Path intermediateSpoonedPath;
    private final Path spoonedPath;
    private final SoraldConfig config;
    private int patchedFileCounter = 0;

    final List<SoraldEventHandler> eventHandlers;

    public Repair(SoraldConfig config, List<? extends SoraldEventHandler> eventHandlers) {
        this.config = config;
        if (this.config.getGitRepoPath() != null) {
            generator.setGitProjectRootDir(this.config.getGitRepoPath());
        }
        spoonedPath = Paths.get(config.getWorkspace()).resolve(Constants.SPOONED);
        intermediateSpoonedPath = spoonedPath.resolve(Constants.INTERMEDIATE);
        this.eventHandlers = Collections.unmodifiableList(eventHandlers);
    }

    /** Execute a repair according to the config. */
    public void repair() {
        UniqueTypesCollector.getInstance().reset();
        List<Integer> ruleKeys = config.getRuleKeys();
        List<SoraldAbstractProcessor<?>> addedProcessors = new ArrayList<>();

        for (int i = 0; i < ruleKeys.size(); i++) {
            int ruleKey = ruleKeys.get(i);

            Pair<Path, Path> inOutPaths = computeInOutPaths(i == 0, i == ruleKeys.size() - 1);
            final Path inputDir = inOutPaths.getLeft();
            final Path outputDir = inOutPaths.getRight();

            Set<RuleViolation> ruleViolations =
                    ProjectScanner.scanProject(
                            inputDir.toFile(),
                            FileUtils.getClosestDirectory(inputDir.toFile()),
                            Checks.getCheckInstance(Integer.toString(ruleKey)));
            SoraldAbstractProcessor<?> processor =
                    createProcessor(ruleKey).setRuleViolations(ruleViolations);
            addedProcessors.add(processor);
            Stream<CtModel> models = repair(inputDir, processor);

            models.forEach(model -> writeModel(model, outputDir));
        }

        printEndProcess(addedProcessors);
        FileUtils.deleteDirectory(intermediateSpoonedPath.toFile());
    }

    Stream<CtModel> repair(Path inputDir, SoraldAbstractProcessor<?> processor) {
        if (config.getRepairStrategy() == RepairStrategy.DEFAULT) {
            CtModel model = defaultRepair(inputDir, processor);
            return Stream.of(model);
        } else {
            assert config.getRepairStrategy() == RepairStrategy.SEGMENT;
            return segmentRepair(inputDir, processor);
        }
    }

    CtModel defaultRepair(Path inputDir, SoraldAbstractProcessor<?> processor) {
        EventHelper.fireEvent(EventType.PARSE_START, eventHandlers);
        Launcher launcher = new Launcher();
        launcher.addInputResource(inputDir.toString());
        CtModel model = initLauncher(launcher).getModel();
        EventHelper.fireEvent(EventType.PARSE_END, eventHandlers);

        EventHelper.fireEvent(EventType.REPAIR_START, eventHandlers);
        repairModelWithInitializedProcessor(model, processor);
        EventHelper.fireEvent(EventType.REPAIR_END, eventHandlers);

        return model;
    }

    Stream<CtModel> segmentRepair(Path inputDir, SoraldAbstractProcessor<?> processor) {
        Node rootNode = SoraldTreeBuilderAlgorithm.buildTree(inputDir.toString());
        LinkedList<LinkedList<Node>> segments =
                FirstFitSegmentationAlgorithm.segment(rootNode, config.getMaxFilesPerSegment());

        return segments.stream()
                .map(
                        segment -> {
                            Launcher launcher = createSegmentLauncher(segment);
                            CtModel model = launcher.getModel();
                            repairModelWithInitializedProcessor(model, processor);
                            return model;
                        })
                .takeWhile(model -> processor.getNbFixes() < config.getMaxFixesPerRule());
    }

    private Pair<Path, Path> computeInOutPaths(boolean isFirstRule, boolean isLastRule) {
        if (isFirstRule && isLastRule) {
            // one processor, straightforward repair: we use the given input file dir, run one
            // processor, directly output files (independently of the FileOutputStrategy) in
            // the final output dir
            return Pair.of(Paths.get(config.getOriginalFilesPath()), spoonedPath);
        } else {
            // more than one processor, thus we need an intermediate dir, which will always
            // contain all files (the changed and non-changed ones), because other processors
            // will run on them
            if (isFirstRule) {
                // the first processor will run, thus we use the given input file
                // dir and output *all* files in the intermediate dir
                return Pair.of(Paths.get(config.getOriginalFilesPath()), intermediateSpoonedPath);
            } else if (isLastRule) {
                // the last processor will run, thus we use as input files the ones in
                // the intermediate dir and output files in the final output dir
                return Pair.of(intermediateSpoonedPath, spoonedPath);
            } else {
                // neither the first nor the last processor will run, thus use as input and output
                // dirs the intermediate dir
                return Pair.of(intermediateSpoonedPath, intermediateSpoonedPath);
            }
        }
    }

    private static void repairModelWithInitializedProcessor(
            CtModel model, SoraldAbstractProcessor<?> processor) {
        Factory factory = model.getUnnamedModule().getFactory();
        ProcessingManager processingManager = new QueueProcessingManager(factory);
        processingManager.addProcessor(processor);
        processingManager.process(factory.Class().getAll());
    }

    private Launcher createSegmentLauncher(List<Node> segment) {
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
        return initLauncher(launcher);
    }

    private void writeModel(CtModel model, Path outputDir) {
        Factory factory = model.getUnnamedModule().getFactory();
        Environment env = factory.getEnvironment();
        env.setOutputDestinationHandler(
                new DefaultOutputDestinationHandler(outputDir.toFile(), env));

        JavaOutputProcessor javaOutputProcessor = new JavaOutputProcessor();
        javaOutputProcessor.setFactory(factory);
        QueueProcessingManager processingManager = new QueueProcessingManager(factory);
        processingManager.addProcessor(javaOutputProcessor);

        boolean isIntermediateOutputDir =
                outputDir.toString().contains(intermediateSpoonedPath.toString());
        if (config.getFileOutputStrategy() == FileOutputStrategy.ALL || isIntermediateOutputDir) {
            processingManager.process(model.getUnnamedModule().getFactory().Class().getAll());
        } else {
            assert (config.getFileOutputStrategy() == FileOutputStrategy.CHANGED_ONLY
                    && !isIntermediateOutputDir);

            for (Map.Entry<String, CtType> patchedFile :
                    UniqueTypesCollector.getInstance().getTopLevelTypes4Output().entrySet()) {
                javaOutputProcessor.process(patchedFile.getValue());
                if (config.getGitRepoPath() != null) {
                    createPatches(patchedFile.getKey(), javaOutputProcessor);
                }
            }
        }
    }

    private void printEndProcess(List<SoraldAbstractProcessor<?>> processors) {
        System.out.println("-----Number of fixes------");
        for (SoraldAbstractProcessor<?> processor : processors) {
            System.out.println(
                    processor.getClass().getSimpleName() + ": " + processor.getNbFixes());
        }
        System.out.println("-----End of report------");
    }

    private void createPatches(String patchedFilePath, JavaOutputProcessor javaOutputProcessor) {
        File patchDir = new File(config.getWorkspace() + File.separator + Constants.PATCHES);

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
                            + patchedFileCounter);
            patchedFileCounter++;
        }
    }

    private Launcher initLauncher(Launcher launcher) {
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

    private SoraldAbstractProcessor<?> createBaseProcessor(Integer ruleKey) {
        try {
            Class<?> processor = Processors.getProcessor(ruleKey);
            if (processor != null) {
                Constructor<?> cons = processor.getConstructor();
                return (SoraldAbstractProcessor<?>) cons.newInstance();
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
            return processor
                    .setMaxFixes(config.getMaxFixesPerRule())
                    .setEventHandlers(eventHandlers);
        }
        return null;
    }
}
