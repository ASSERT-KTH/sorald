import spoon.Launcher;
import spoon.processing.Processor;
import spoon.support.sniper.SniperJavaPrettyPrinter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.lang.reflect.Constructor;

public class TestHelp {

    public static void repair(String pathToFile, String projectKey, int rulekey) throws Exception
    {
        Launcher launcher = new Launcher() {
        };
        launcher.getEnvironment().setPrettyPrinterCreator(() -> {
            SniperJavaPrettyPrinter sniper = new SniperJavaPrettyPrinter(launcher.getEnvironment());
            sniper.setIgnoreImplicit(false);
                    return sniper;
                }
        );
        launcher.addInputResource(pathToFile);
        launcher.getEnvironment().setCommentEnabled(true);
        launcher.getEnvironment().setAutoImports(true);
        launcher.getEnvironment().useTabulations(true);
        launcher.getEnvironment().setTabulationSize(4);

        Class<?> processor = Processors.getProcessor(rulekey);
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
        Class<?> processor = Processors.getProcessor(rulekey);
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
                    line = line.substring(0, line.indexOf(complianceComment));
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
