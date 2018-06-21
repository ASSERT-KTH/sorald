import org.json.JSONArray;
import spoon.Launcher;
import spoon.experimental.modelobs.SourceFragmentsTreeCreatingChangeCollector;
import spoon.processing.Processor;

import spoon.reflect.visitor.PrettyPrinter;
import spoon.reflect.visitor.printer.change.ChangesAwareDefaultJavaPrettyPrinter;




import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.lang.System.exit;
import static java.lang.Thread.sleep;

public class TestHelp {

    public static Map<Integer, Class<? extends Processor>> rule;

    public static void initmap() {
        rule = new HashMap<>();
        rule.putIfAbsent(1854, DeadStoreProcessor.class);
        rule.putIfAbsent(1948, SerializableFieldProcessor.class);
        rule.putIfAbsent(2055, NonSerializableSuperClassProcessor.class);
        rule.putIfAbsent(2095, ResourceCloseProcessor.class);
        rule.putIfAbsent(2259, NullDereferenceProcessor.class);
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

    public static void repair(String pathToFile, String projectKey, int rulekey) throws Exception {

        //Not Sniper  Mode
        Launcher launcher = new Launcher() {
        	@Override
        	public PrettyPrinter createPrettyPrinter() {
        		return new ChangesAwareDefaultJavaPrettyPrinter(getEnvironment());
        	}
        	@Override
        	public void process() {
                new SourceFragmentsTreeCreatingChangeCollector().attachTo(factory.getEnvironment());
        		super.process();
        	}
        };

        launcher.addInputResource(pathToFile);

        launcher.getEnvironment().setCommentEnabled(true);
        launcher.getEnvironment().setAutoImports(true);
//        launcher.getEnvironment().useTabulations(true);
//        launcher.getEnvironment().setTabulationSize(4);

        Class<?> processor = getProcessor(rulekey);
        Constructor<?> cons = processor.getConstructor(String.class);
        Object object = cons.newInstance(projectKey);
//        launcher.addProcessor((Processor) object);
        launcher.run();
//        new SpoonModelTree(launcher.getFactory());
    }

    public static boolean checkBugs(String pathToFile, int rulekey) throws Exception {
        String cdrep = "./src/test/sonarepaired/";
        String cdtest = "./src/test/sonatest/";

        String arr[] = pathToFile.split("/");
        String fileName = arr[arr.length - 1];

        copy("./spooned/" + fileName, cdrep + pathToFile);

        Execute.command("mvn clean package", cdrep, true);

        doSonarAnalysis(cdrep);

        sleep((long) 1000 * 10);

        JSONArray array = ParseAPI.parse(rulekey, "", "se.kth:sonarepaired");

        copy(cdtest + pathToFile, cdrep + pathToFile);//copy file back from sonatest to sonarepair

        Execute.command("rm -rf ./spooned/");

        if (array.length() == 0) {
            return false;
        } else {
            Set<Bug> setOfBugs = Bug.createSetOfBugs(array);
            for (Bug bug : setOfBugs) {
                String bugFileName = bug.getFileName();
                if (bugFileName.equals(fileName)) {
                    System.out.println(bug.getJsonObject().toString());
                    return true;
                }
            }
            return false;
        }
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

