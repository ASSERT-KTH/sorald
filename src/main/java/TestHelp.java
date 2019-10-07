import spoon.Launcher;
import spoon.processing.Processor;
import spoon.support.sniper.SniperJavaPrettyPrinter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.exit;

public class TestHelp {

    private static Map<Integer, Class<? extends Processor>> rule;

    public static void initmap() {
        rule = new HashMap<>();
        rule.putIfAbsent(1854, DeadStoreProcessor.class);
        rule.putIfAbsent(1948, SerializableFieldProcessor.class);
        // rule.putIfAbsent(2055, NonSerializableSuperClassProcessor.class);
        rule.putIfAbsent(2095, ResourceCloseProcessor.class);
        rule.putIfAbsent(2116, ArrayToStringProcessor.class);
        // rule.putIfAbsent(2259, NullDereferenceProcessor.class);
        rule.putIfAbsent(2272, NoSuchElementProcessor.class);
        rule.putIfAbsent(4973, BoxedTypesEqualsProcessor.class);
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

    public static void repair(String pathToFile, String projectKey, int rulekey) throws Exception
    {
        Launcher launcher = new Launcher() {
        };
        launcher.getEnvironment().setPrettyPrinterCreator(() -> {
                    return new SniperJavaPrettyPrinter(launcher.getEnvironment());
                }
        );
        launcher.addInputResource(pathToFile);
        launcher.getEnvironment().setCommentEnabled(true);
        launcher.getEnvironment().setAutoImports(true);
        launcher.getEnvironment().useTabulations(true);
        launcher.getEnvironment().setTabulationSize(4);

        Class<?> processor = getProcessor(rulekey);
        Constructor<?> cons = processor.getConstructor(String.class);
        Object object = cons.newInstance(projectKey);
        launcher.addProcessor((Processor) object);
        launcher.run();
    }

    public static void normalRepair(String pathToFile, String projectKey, int rulekey) throws Exception {
        //Not Sniper  Mode
        Launcher launcher = new Launcher();
        launcher.addInputResource(pathToFile);
        launcher.getEnvironment().setAutoImports(true);
        Class<?> processor = getProcessor(rulekey);
        Constructor<?> cons = processor.getConstructor(String.class);
        Object object = cons.newInstance(projectKey);
        launcher.addProcessor((Processor) object);
        launcher.run();
//        new SpoonModelTree(launcher.getFactory());
    }

    /*
    Simple helper method that removes the mandatory // Noncompliant comments from test files.
     */
    public static void removeComplianceComments(String pathToRepairedFile) {
        final String complianceComment = "// Noncompliant";
        try {
            BufferedReader file = new BufferedReader(new FileReader(pathToRepairedFile));
            StringBuffer inputBuffer = new StringBuffer();
            String line;

            while ((line = file.readLine()) != null) {
                if(line.contains(complianceComment)){
                    line.trim();
                    line = line.substring(0, line.length() - (complianceComment.length()));
                }
                inputBuffer.append(line+'\n');
            }
            file.close();
            FileOutputStream fileOut = new FileOutputStream(pathToRepairedFile);
            fileOut.write(inputBuffer.toString().getBytes());
            fileOut.close();

        } catch (Exception e) {
            System.out.println("Problem reading file.");
        }
    }

    public static void copy(String from, String to) {
        Execute.command("cp " + from + " " + to);
    }

    public static void doSonarAnalysis(String dir) {
        String sonarAnalysis = "mvn sonar:sonar -Dsonar.organization=kimsun1598-github -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=95b00d7c27acc6500c2641efe2b497442368d037";
        Execute.command(sonarAnalysis, dir, true);
    }
}
