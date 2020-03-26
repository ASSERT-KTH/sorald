package sonarquberepair;

import spoon.Launcher;
import spoon.processing.Processor;
import spoon.support.sniper.SniperJavaPrettyPrinter;

import java.lang.reflect.Constructor;

public class Main {

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
        Constructor<?> cons;
        Object object;
        try {
            cons = processor.getConstructor(String.class);
            object = cons.newInstance(projectKey);
        } catch (NoSuchMethodException e) {
            cons = processor.getConstructor();
            object = cons.newInstance();
        }
        launcher.addProcessor((Processor) object);
        launcher.run();
    }

    public static void normalRepair(String pathToFile, String projectKey, int rulekey) throws Exception {
        //Not Sniper  Mode
        Launcher launcher = new Launcher();
        launcher.addInputResource(pathToFile);
        launcher.getEnvironment().setAutoImports(true);
        Class<?> processor = Processors.getProcessor(rulekey);
        Constructor<?> cons;
        Object object;
        try {
            cons = processor.getConstructor(String.class);
            object = cons.newInstance(projectKey);
        } catch (NoSuchMethodException e) {
            cons = processor.getConstructor();
            object = cons.newInstance();
        }
        launcher.addProcessor((Processor) object);
        launcher.run();
//        new SpoonModelTree(launcher.getFactory());
    }

    /**
     *
     * @param args string array. Give either 0, 1 or 2 arguments. first argument is sonarqube rule-number which you can get from https://rules.sonarsource.com/java/type/Bug
     *             second argument is the projectKey for the sonarqube analysis of source files. for  example "fr.inria.gforge.spoon:spoon-core"
     */
    public static void main(String[] args) throws Exception
    {

        String projectKey="fr.inria.gforge.spoon:spoon-core";
        int rulenumber = 2116;
        if(args.length>0)
        {
            rulenumber = Integer.parseInt(args[0]);

            if(args.length==1)
            {
                projectKey = "fr.inria.gforge.spoon:spoon-core";
                System.out.println("One argument given. Applying " + Processors.getProcessor(rulenumber).getName() + " on " + projectKey);
            }
            else if(args.length==2)
            {
                projectKey = args[1];
                System.out.println("Two argument given. Applying "+Processors.getProcessor(rulenumber).getName()+ " on "+projectKey);
            }
            else
            {
                throw new IllegalArgumentException("Enter less than three arguments");
            }
        }
        else //no arguments given
        {
            System.out.println("No arguments given. Using "+ Processors.getProcessor(rulenumber).getName()+ " by default on "+projectKey);
        }
        repair("./source/act/",projectKey,rulenumber);
        System.out.println("done");
	}
}
