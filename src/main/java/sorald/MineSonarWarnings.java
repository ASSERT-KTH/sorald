package sorald;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.stringparsers.BooleanStringParser;
import com.martiansoftware.jsap.stringparsers.FileStringParser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.sonar.java.AnalyzerMessage;
import org.sonar.java.checks.*;
import org.sonar.java.checks.naming.MethodNamedEqualsCheck;
import org.sonar.java.checks.naming.MethodNamedHashcodeOrEqualCheck;
import org.sonar.java.checks.serialization.CustomSerializationMethodCheck;
import org.sonar.java.checks.serialization.ExternalizableClassConstructorCheck;
import org.sonar.java.checks.serialization.SerializableObjectInSessionCheck;
import org.sonar.java.checks.serialization.SerializableSuperConstructorCheck;
import org.sonar.java.checks.spring.ControllerWithSessionAttributesCheck;
import org.sonar.java.checks.spring.SpringComponentWithWrongScopeCheck;
import org.sonar.java.checks.spring.SpringIncompatibleTransactionalCheck;
import org.sonar.java.checks.spring.SpringScanDefaultPackageCheck;
import org.sonar.java.checks.synchronization.DoubleCheckedLockingCheck;
import org.sonar.java.checks.synchronization.SynchronizationOnGetClassCheck;
import org.sonar.java.checks.synchronization.TwoLocksWaitCheck;
import org.sonar.java.checks.synchronization.ValueBasedObjectUsedForLockCheck;
import org.sonar.java.checks.unused.UnusedReturnedDataCheck;
import org.sonar.java.checks.unused.UnusedThrowableCheck;
import org.sonar.java.checks.verifier.MultipleFilesJavaCheckVerifier;
import org.sonar.java.se.checks.*;
import org.sonar.plugins.java.api.JavaFileScanner;

public class MineSonarWarnings {

    private static final List<JavaFileScanner> SONAR_CHECKS = init();

    private static List init() {
        List<JavaFileScanner> TEMP_SONAR_CHECKS = new ArrayList<>();
        TEMP_SONAR_CHECKS.add(new ControllerWithSessionAttributesCheck());
        TEMP_SONAR_CHECKS.add(new SpringScanDefaultPackageCheck());
        TEMP_SONAR_CHECKS.add(new TwoLocksWaitCheck());
        TEMP_SONAR_CHECKS.add(new PreparedStatementAndResultSetCheck());
        TEMP_SONAR_CHECKS.add(new ThreadSleepCheck());
        TEMP_SONAR_CHECKS.add(new PrintfFailCheck());
        TEMP_SONAR_CHECKS.add(new ThreadWaitCallCheck());
        TEMP_SONAR_CHECKS.add(new SpringIncompatibleTransactionalCheck());
        TEMP_SONAR_CHECKS.add(new DoubleCheckedLockingCheck());
        TEMP_SONAR_CHECKS.add(new GettersSettersOnRightFieldCheck());
        TEMP_SONAR_CHECKS.add(new RunFinalizersCheck());
        TEMP_SONAR_CHECKS.add(new ScheduledThreadPoolExecutorZeroCheck());
        TEMP_SONAR_CHECKS.add(new ReuseRandomCheck());
        TEMP_SONAR_CHECKS.add(new ObjectFinalizeOverloadedCheck());
        TEMP_SONAR_CHECKS.add(new ReturnInFinallyCheck());
        TEMP_SONAR_CHECKS.add(new ThreadLocalCleanupCheck());
        TEMP_SONAR_CHECKS.add(new CompareStringsBoxedTypesWithEqualsCheck());
        TEMP_SONAR_CHECKS.add(new InputStreamReadCheck());
        TEMP_SONAR_CHECKS.add(new CompareToNotOverloadedCheck());
        TEMP_SONAR_CHECKS.add(new IterableIteratorCheck());
        TEMP_SONAR_CHECKS.add(new OverwrittenKeyCheck());
        TEMP_SONAR_CHECKS.add(new DateFormatWeekYearCheck());
        TEMP_SONAR_CHECKS.add(new UnusedThrowableCheck());
        TEMP_SONAR_CHECKS.add(new CollectionSizeAndArrayLengthCheck());
        TEMP_SONAR_CHECKS.add(new AllBranchesAreIdenticalCheck());
        TEMP_SONAR_CHECKS.add(new SynchronizedOverrideCheck());
        TEMP_SONAR_CHECKS.add(new ValueBasedObjectUsedForLockCheck());
        TEMP_SONAR_CHECKS.add(new AssertOnBooleanVariableCheck());
        TEMP_SONAR_CHECKS.add(new VolatileVariablesOperationsCheck());
        TEMP_SONAR_CHECKS.add(new SynchronizationOnGetClassCheck());
        TEMP_SONAR_CHECKS.add(new DoubleCheckedLockingAssignmentCheck());
        TEMP_SONAR_CHECKS.add(new StringCallsBeyondBoundsCheck());
        TEMP_SONAR_CHECKS.add(new RawByteBitwiseOperationsCheck());
        TEMP_SONAR_CHECKS.add(new SyncGetterAndSetterCheck());
        TEMP_SONAR_CHECKS.add(new StaticMultithreadedUnsafeFieldsCheck());
        TEMP_SONAR_CHECKS.add(new NullShouldNotBeUsedWithOptionalCheck());
        TEMP_SONAR_CHECKS.add(new DoublePrefixOperatorCheck());
        TEMP_SONAR_CHECKS.add(new WrongAssignmentOperatorCheck());
        TEMP_SONAR_CHECKS.add(new UnusedReturnedDataCheck());
        TEMP_SONAR_CHECKS.add(new InappropriateRegexpCheck());
        TEMP_SONAR_CHECKS.add(new NotifyCheck());
        TEMP_SONAR_CHECKS.add(new SynchronizedFieldAssignmentCheck());
        TEMP_SONAR_CHECKS.add(new SerializableObjectInSessionCheck());
        TEMP_SONAR_CHECKS.add(new WaitInSynchronizeCheck());
        TEMP_SONAR_CHECKS.add(new ForLoopFalseConditionCheck());
        TEMP_SONAR_CHECKS.add(new ForLoopIncrementSignCheck());
        TEMP_SONAR_CHECKS.add(new TransactionalMethodVisibilityCheck());
        TEMP_SONAR_CHECKS.add(new ServletInstanceFieldCheck());
        TEMP_SONAR_CHECKS.add(new ToStringReturningNullCheck());
        TEMP_SONAR_CHECKS.add(new EqualsOnAtomicClassCheck());
        TEMP_SONAR_CHECKS.add(new IgnoredReturnValueCheck());
        TEMP_SONAR_CHECKS.add(new ConfusingOverloadCheck());
        TEMP_SONAR_CHECKS.add(new CollectionInappropriateCallsCheck());
        TEMP_SONAR_CHECKS.add(new SillyEqualsCheck());
        TEMP_SONAR_CHECKS.add(new PrimitiveWrappersInTernaryOperatorCheck());
        TEMP_SONAR_CHECKS.add(new InterruptedExceptionCheck());
        TEMP_SONAR_CHECKS.add(new ThreadOverridesRunCheck());
        TEMP_SONAR_CHECKS.add(new LongBitsToDoubleOnIntCheck());
        TEMP_SONAR_CHECKS.add(new UselessIncrementCheck());
        TEMP_SONAR_CHECKS.add(new SillyStringOperationsCheck());
        TEMP_SONAR_CHECKS.add(new NonSerializableWriteCheck());
        TEMP_SONAR_CHECKS.add(new ArrayHashCodeAndToStringCheck());
        TEMP_SONAR_CHECKS.add(new CollectionCallingItselfCheck());
        TEMP_SONAR_CHECKS.add(new BigDecimalDoubleConstructorCheck());
        TEMP_SONAR_CHECKS.add(new InvalidDateValuesCheck());
        TEMP_SONAR_CHECKS.add(new ReflectionOnNonRuntimeAnnotationCheck());
        TEMP_SONAR_CHECKS.add(new CustomSerializationMethodCheck());
        TEMP_SONAR_CHECKS.add(new ExternalizableClassConstructorCheck());
        TEMP_SONAR_CHECKS.add(new ClassComparedByNameCheck());
        TEMP_SONAR_CHECKS.add(new DuplicateConditionIfElseIfCheck());
        TEMP_SONAR_CHECKS.add(new SynchronizationOnStringOrBoxedCheck());
        TEMP_SONAR_CHECKS.add(new HasNextCallingNextCheck());
        TEMP_SONAR_CHECKS.add(new IdenticalOperandOnBinaryExpressionCheck());
        TEMP_SONAR_CHECKS.add(new LoopExecutingAtMostOnceCheck());
        TEMP_SONAR_CHECKS.add(new SelfAssignementCheck());
        TEMP_SONAR_CHECKS.add(new StringBufferAndBuilderWithCharCheck());
        TEMP_SONAR_CHECKS.add(new MethodNamedHashcodeOrEqualCheck());
        TEMP_SONAR_CHECKS.add(new ThreadRunCheck());
        TEMP_SONAR_CHECKS.add(new MethodNamedEqualsCheck());
        TEMP_SONAR_CHECKS.add(new DoubleBraceInitializationCheck());
        TEMP_SONAR_CHECKS.add(new VolatileNonPrimitiveFieldCheck());
        TEMP_SONAR_CHECKS.add(new ToArrayCheck());
        TEMP_SONAR_CHECKS.add(new AbsOnNegativeCheck());
        TEMP_SONAR_CHECKS.add(new IgnoredStreamReturnValueCheck());
        TEMP_SONAR_CHECKS.add(new IteratorNextExceptionCheck());
        TEMP_SONAR_CHECKS.add(new CompareToResultTestCheck());
        TEMP_SONAR_CHECKS.add(new CastArithmeticOperandCheck());
        TEMP_SONAR_CHECKS.add(new ShiftOnIntOrLongCheck());
        TEMP_SONAR_CHECKS.add(new CompareToReturnValueCheck());
        TEMP_SONAR_CHECKS.add(new ImmediateReverseBoxingCheck());
        TEMP_SONAR_CHECKS.add(new EqualsArgumentTypeCheck());
        TEMP_SONAR_CHECKS.add(new InnerClassOfNonSerializableCheck());
        TEMP_SONAR_CHECKS.add(new SerializableSuperConstructorCheck());
        TEMP_SONAR_CHECKS.add(new ParameterReassignedToCheck());
        TEMP_SONAR_CHECKS.add(new EqualsOverridenWithHashCodeCheck());
        TEMP_SONAR_CHECKS.add(new ObjectFinalizeOverridenCallsSuperFinalizeCheck());
        TEMP_SONAR_CHECKS.add(new SpringComponentWithWrongScopeCheck());
        TEMP_SONAR_CHECKS.add(new ConstructorInjectionCheck());
        TEMP_SONAR_CHECKS.add(new ClassWithoutHashCodeInHashStructureCheck());
        TEMP_SONAR_CHECKS.add(new InstanceOfAlwaysTrueCheck());
        TEMP_SONAR_CHECKS.add(new NullDereferenceInConditionalCheck());
        TEMP_SONAR_CHECKS.add(new FloatEqualityCheck());
        TEMP_SONAR_CHECKS.add(new IfConditionAlwaysTrueOrFalseCheck());
        TEMP_SONAR_CHECKS.add(new ObjectFinalizeCheck());
        TEMP_SONAR_CHECKS.add(new GetClassLoaderCheck());
        TEMP_SONAR_CHECKS.add(new MathOnFloatCheck());
        TEMP_SONAR_CHECKS.add(new SymmetricEqualsCheck());

        TEMP_SONAR_CHECKS.add(new ObjectOutputStreamCheck());
        TEMP_SONAR_CHECKS.add(new NoWayOutLoopCheck());
        TEMP_SONAR_CHECKS.add(new UnclosedResourcesCheck());
        TEMP_SONAR_CHECKS.add(new DivisionByZeroCheck());
        TEMP_SONAR_CHECKS.add(new LocksNotUnlockedCheck());
        TEMP_SONAR_CHECKS.add(new StreamConsumedCheck());
        TEMP_SONAR_CHECKS.add(new StreamNotConsumedCheck());
        TEMP_SONAR_CHECKS.add(new OptionalGetBeforeIsPresentCheck());
        TEMP_SONAR_CHECKS.add(new MinMaxRangeCheck());
        TEMP_SONAR_CHECKS.add(new ConditionalUnreachableCodeCheck());
        TEMP_SONAR_CHECKS.add(new NullDereferenceCheck());
        TEMP_SONAR_CHECKS.add(new NonNullSetToNullCheck());
        TEMP_SONAR_CHECKS.add(new CustomUnclosedResourcesCheck());

//        TEMP_SONAR_CHECKS.add(new DefaultMessageListenerContainerCheck());
//        TEMP_SONAR_CHECKS.add(new SingleConnectionFactoryCheck());
//        TEMP_SONAR_CHECKS.add(new DependencyWithSystemScopeCheck());

        return TEMP_SONAR_CHECKS;
    }

    public static JSAP defineArgs() throws JSAPException {
        JSAP jsap = new JSAP();

        FlaggedOption opt = new FlaggedOption(Constants.ARG_ORIGINAL_FILES_PATH);
        opt.setLongFlag(Constants.ARG_ORIGINAL_FILES_PATH);
        opt.setStringParser(FileStringParser.getParser().setMustExist(true));
        opt.setRequired(false);
        opt.setHelp("The path to the file or folder to be analyzed.");
        jsap.registerParameter(opt);

        opt = new FlaggedOption(Constants.ARG_STATS_ON_GIT_REPOS);
        opt.setLongFlag(Constants.ARG_STATS_ON_GIT_REPOS);
        opt.setStringParser(BooleanStringParser.getParser());
        opt.setRequired(false);
        opt.setHelp("If the stats should be computed on git repos.");
        jsap.registerParameter(opt);

        opt = new FlaggedOption(Constants.ARG_STATS_OUTPUT_FILE);
        opt.setLongFlag(Constants.ARG_STATS_OUTPUT_FILE);
        opt.setStringParser(FileStringParser.getParser().setMustExist(true));
        opt.setRequired(false);
        opt.setHelp("The path to the output file.");
        jsap.registerParameter(opt);

        opt = new FlaggedOption(Constants.ARG_GIT_REPOS_LIST);
        opt.setLongFlag(Constants.ARG_GIT_REPOS_LIST);
        opt.setStringParser(FileStringParser.getParser().setMustExist(true));
        opt.setRequired(false);
        opt.setHelp("The path to the repos list.");
        jsap.registerParameter(opt);

        opt = new FlaggedOption(Constants.ARG_TEMP_DIR);
        opt.setLongFlag(Constants.ARG_TEMP_DIR);
        opt.setStringParser(FileStringParser.getParser().setMustExist(true));
        opt.setRequired(false);
        opt.setHelp("The path to the temp directory.");
        jsap.registerParameter(opt);

        Switch sw = new Switch("help");
        sw.setShortFlag('h');
        sw.setLongFlag("help");
        sw.setDefault("false");
        jsap.registerParameter(sw);

        return jsap;
    }

    public static void checkArguments(JSAP jsap, JSAPResult arguments) {
        if (!arguments.success()) {
            for (java.util.Iterator<?> errors = arguments.getErrorMessageIterator(); errors.hasNext(); ) {
                System.err.println("Error: " + errors.next());
            }
            printUsage(jsap);
        }

        if (arguments.getBoolean("help")) {
            printUsage(jsap);
        }
    }

    public static void printUsage(JSAP jsap) {
        System.err.println("Arguments: ");
        System.err.println();
        System.err.println(jsap.getHelp());
        System.exit(-1);
    }

    public static void main(String[] args) throws JSAPException, IOException, GitAPIException {
        JSAP jsap = defineArgs();
        JSAPResult arguments = jsap.parse(args);
        checkArguments(jsap, arguments);

        if (arguments.contains(Constants.ARG_STATS_ON_GIT_REPOS)) {
            // stats on a list of git repos
            String outputPath = arguments.getFile(Constants.ARG_STATS_OUTPUT_FILE).getAbsolutePath();
            String reposListFilePath =
                    arguments.getFile(Constants.ARG_GIT_REPOS_LIST).getAbsolutePath();
            File repoDir = new File(arguments.getFile(Constants.ARG_TEMP_DIR).getAbsolutePath());

            List<String> reposList = getReposList(reposListFilePath);

            for (String repo : reposList) {
                String repoName = repo.substring(repo.lastIndexOf('/') + 1, repo.lastIndexOf("."));

                FileUtils.cleanDirectory(repoDir);

                boolean isCloned = false;

                try {
                    Git git = Git.cloneRepository()
                            .setURI(repo)
                            .setDirectory(repoDir)
                            .call();
                    git.close();
                    isCloned = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Map<String, Integer> warnings = extractWarnings(repoDir.getAbsolutePath());

                PrintWriter pw = new PrintWriter(new FileWriter(outputPath, true));

                if (isCloned) {
                    pw.println("RepoName: " + repoName);

                    warnings.entrySet().stream()
                            .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                            .forEach(pw::println);
                } else {
                    pw.println("RepoName: " + repoName + " not_cloned");
                }

                pw.flush();
                pw.close();
            }

        } else { // default mode

            String projectPath = arguments.getFile(Constants.ARG_ORIGINAL_FILES_PATH).getAbsolutePath();

            Map<String, Integer> warnings = extractWarnings(projectPath);

            warnings.entrySet().stream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                    .forEach(System.out::println);

        }
    }

    private static List<String> getReposList(String reposListFilePath) throws FileNotFoundException {
        List<String> res = new ArrayList<>();

        Scanner sc = new Scanner(new File(reposListFilePath));

        while (sc.hasNextLine()) {
            res.add(sc.nextLine());
        }

        sc.close();

        return res;
    }

    private static Map<String, Integer> extractWarnings(String projectPath) {
        Map<String, Integer> warnings = new HashMap<>();

        try {
            List<String> filesToScan = new ArrayList<>();
            File file = new File(projectPath);
            if (file.isFile()) {
                filesToScan.add(file.getAbsolutePath());
            } else {
                try (Stream<Path> walk = Files.walk(Paths.get(file.getAbsolutePath()))) {
                    filesToScan = walk.map(x -> x.toFile().getAbsolutePath())
                            .filter(f -> f.endsWith(Constants.JAVA_EXT)).collect(Collectors.toList());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            for (JavaFileScanner javaFileScanner : SONAR_CHECKS) {
                Set<AnalyzerMessage> issues = MultipleFilesJavaCheckVerifier.verify(filesToScan, javaFileScanner, false);
                warnings.putIfAbsent(javaFileScanner.getClass().getSimpleName(), issues.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return warnings;
    }

}
