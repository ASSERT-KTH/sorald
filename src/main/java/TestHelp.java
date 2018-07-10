import org.sonar.java.AnalyzerMessage;
import org.sonar.java.checks.DeadStoreCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import spoon.Launcher;
import spoon.experimental.modelobs.SourceFragmentsTreeCreatingChangeCollector;
import spoon.processing.Processor;
import spoon.reflect.visitor.PrettyPrinter;
import spoon.reflect.visitor.printer.change.ChangesAwareDefaultJavaPrettyPrinter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static java.lang.System.exit;

public class TestHelp {

    private static Map<Integer, Class<? extends Processor>> rule;

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

    public static void repair(String pathToFile, String projectKey, int rulekey) throws Exception
    {
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
        launcher.getEnvironment().useTabulations(true);
        launcher.getEnvironment().setTabulationSize(4);

        String[] ext = new String[1];
        ext[0]="java";
        Path testJars = Paths.get(pathToFile);
        List<File> files = getFilesRecursively(testJars, ext);

        Class<?> processor = getProcessor(rulekey);
        Constructor<?> cons = processor.getConstructor(List.class);
        Object object = cons.newInstance(files);
        launcher.addProcessor((Processor) object);
        launcher.run();
//        new SpoonModelTree(launcher.getFactory());
    }

    public static void normalRepair(String pathToFile, String projectKey, int rulekey) throws Exception {
        //Not Sniper  Mode
        Launcher launcher = new Launcher();

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
//        new SpoonModelTree(launcher.getFactory());
    }

    public static void copy(String from, String to) {
        Execute.command("cp " + from + " " + to);
    }

    public static void doSonarAnalysis(String dir) {
        String sonarAnalysis = "mvn sonar:sonar -Dsonar.organization=kimsun1598-github -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=95b00d7c27acc6500c2641efe2b497442368d037";
        Execute.command(sonarAnalysis, dir, true);
    }
    static List<File> getFilesRecursively(Path root, final String[] extensions) {
        final List<File> files = new ArrayList();
        SimpleFileVisitor visitor = new SimpleFileVisitor<Path>() {
            public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) {
                String[] var3 = extensions;
                int var4 = var3.length;

                for(int var5 = 0; var5 < var4; ++var5) {
                    String extension = var3[var5];
                    if (filePath.toString().endsWith("." + extension)) {
                        files.add(filePath.toFile());
                        break;
                    }
                }

                return FileVisitResult.CONTINUE;
            }

            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        };

        try {
            Files.walkFileTree(root, visitor);
        } catch (IOException var5) {
            ;
        }

        return files;
    }
}
