import spoon.Launcher;
import spoon.processing.Processor;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;



public class Main {



    /**
     *
     * @param args string array. Give either 0, 1 or 2 arguments. first argument is sonarqube rule-number which you can get from https://rules.sonarsource.com/java/type/Bug
     *             second argument is the projectKey for the sonarqube analysis of source files. for  example "fr.inria.gforge.spoon:spoon-core"
     */
    public static void main(String[] args) throws Exception
    {
        System.out.println();
        //Not Sniper  Mode
		Launcher launcher = new Launcher();
		launcher.addInputResource("./source/act/");
        launcher.getEnvironment().setCommentEnabled(true);
        launcher.getEnvironment().setAutoImports(true);
//        launcher.getEnvironment().setPreserveLineNumbers(true);
        launcher.getEnvironment().setTabulationSize(4);
        launcher.getEnvironment().useTabulations(true);

        String projectKey="fr.inria.gforge.spoon:spoon-core";
        Class<?> processor;
        if(args.length>0)
        {
            int rulenumber = Integer.parseInt(args[0]);

            processor=TestHelp.getProcessor(rulenumber);

            if(args.length==1)
            {
                projectKey="fr.inria.gforge.spoon:spoon-core";
                System.out.println("One argument given. Applying "+processor.getName()+ " on spoon-core");
            }
            else if(args.length==2)
            {
                projectKey = args[1];
                System.out.println("Two argument given. Applying "+processor.getName()+ " on "+projectKey);
            }
            else
            {
                throw new IllegalArgumentException("Enter only one or two arguments");
            }
        }
        else //no arguments given
        {
            processor=TestHelp.getProcessor(1948);//default DeadstoreProcessor
            projectKey="fr.inria.gforge.spoon:spoon-core";
            System.out.println("No arguments given. Using "+ processor.getName()+ " by default on spoon-core");
        }
        Constructor<?> cons = processor.getConstructor(String.class);
        Object object = cons.newInstance(projectKey);
        launcher.addProcessor( (Processor)object);
		launcher.run();

//	   	new SpoonModelTree(launcher.getFactory());

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

        System.out.println("done");
	}
}