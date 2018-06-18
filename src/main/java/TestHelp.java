import org.sonar.java.checks.DeadStoreCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import org.sonar.java.se.checks.NullDereferenceCheck;
import org.sonar.plugins.java.api.JavaFileScanner;
import spoon.Launcher;
import spoon.experimental.modelobs.SourceFragmentsTreeCreatingChangeCollector;
import spoon.processing.Processor;
import spoon.reflect.factory.Factory;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.exit;

public class TestHelp {

    public static Map<Integer, Class<? extends Processor>> rule;
    public static Map<Integer, Class<? extends JavaFileScanner>> rulecheck;

    public static void initmap() {
        rule = new HashMap<>();
        rule.putIfAbsent(1854, DeadStoreProcessor.class);
        rule.putIfAbsent(1948, SerializableFieldProcessor.class);
        rule.putIfAbsent(2055, NonSerializableSuperClassProcessor.class);
        rule.putIfAbsent(2095, ResourceCloseProcessor.class);
        rule.putIfAbsent(2259, NullDereferenceProcessor.class);
        rulecheck = new HashMap<>();
        rulecheck.putIfAbsent(1854, DeadStoreCheck.class);
//        rulecheck.putIfAbsent(1948, SerializableFieldProcessor.class);
//        rulecheck.putIfAbsent(2055, NonSerializableSuperClassProcessor.class);
//        rulecheck.putIfAbsent(2095, ResourceCloseProcessor.class);
        rulecheck.putIfAbsent(2259, NullDereferenceCheck.class);
    }

    public static Class<?> getProcessor(int ruleKey) {
        if (rule == null) {
            initmap();
        }
        if (!rule.containsKey(ruleKey)) {
            System.out.println("Sorry. Repair not available for rule " + ruleKey);
            exit(0);
        }
        return rule.get(ruleKey);
    }
    public static Class<?> getChecker(int ruleKey) {
        if (rulecheck == null) {
            initmap();
        }
        if (!rulecheck.containsKey(ruleKey)) {
            System.out.println("Sorry. Checker not available for rule " + ruleKey);
            exit(0);
        }
        return rulecheck.get(ruleKey);
    }

    public static void repair(String pathToFile, String projectKey, int rulekey) throws Exception {

        //Not Sniper  Mode
        Launcher launcher = new Launcher();

        launcher.addInputResource(pathToFile);

        launcher.getEnvironment().setCommentEnabled(true);
        launcher.getEnvironment().setAutoImports(true);
        launcher.getEnvironment().setTabulationSize(4);
        launcher.getEnvironment().useTabulations(true);
        
        Factory factory = launcher.getFactory();
        
		new SourceFragmentsTreeCreatingChangeCollector().attachTo(factory.getEnvironment());

        Class<?> processor = getProcessor(rulekey);
        Constructor<?> cons = processor.getConstructor(String.class);
        Object object = cons.newInstance(projectKey);
        launcher.addProcessor((Processor) object);
        launcher.run();
//        new SpoonModelTree(launcher.getFactory());
    }

    public static boolean checkBugs(String pathToFile, int rulekey) throws Exception {

        String arr[] = pathToFile.split("/");
        String fileName = arr[arr.length - 1];

        Class<?> checker = getChecker(rulekey);
        Constructor<?> cons = checker.getConstructor();
        Object object = cons.newInstance();

        JavaCheckVerifier.verify(pathToFile, (JavaFileScanner) object);
        return true;
    }

    public static void copy(String from, String to) {
        Execute.command("cp " + from + " " + to);
    }

    public static void doSonarAnalysis(String dir) {
        String sonarAnalysis = "mvn sonar:sonar -Dsonar.organization=kimsun1598-github -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=95b00d7c27acc6500c2641efe2b497442368d037";
        Execute.command(sonarAnalysis, dir, true);
    }

    public static boolean hasSonarBug(String pathToFile, int rulekey) throws Exception {
        return checkBugs(pathToFile, rulekey);
    }
}
        /*
        //Sniper Mode . Add pavel's refDJPP branch of spoon as library to use this.
        Launcher launcher = new Launcher();
        launcher.addInputResource("/home/ashutosh/eclipse-workspace/spoon1/source/act/");
        launcher.getEnvironment().setCommentEnabled(true);
        launcher.getEnvironment().setAutoImports(true);
        launcher.buildModel();
        Factory f = launcher.getFactory();
        new SourceFragmentsTreeCreatingChangeCollector().attachTo(f.getEnvironment());

        CtClass<?> ctClass = launcher.getFactory().Class().get(JDTTreeBuilderHelper.class);
//        SniperHelper.process(ctClass);

        ChangesAwareDefaultJavaPrettyPrinter printer = new ChangesAwareDefaultJavaPrettyPrinter(f.getEnvironment());
        CompilationUnit cu = f.CompilationUnit().getOrCreate(ctClass);
        List<CtType<?>> toBePrinted = new ArrayList<>();
        toBePrinted.add(ctClass);
        printer.calculate(cu, toBePrinted);
        */

