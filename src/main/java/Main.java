import spoon.Launcher;
import spoon.processing.Processor;
import spoon.support.gui.SpoonModelTree;

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class Main {
	public static void main(String[] args) throws Exception
    {
        /*
        String command = "pwd";
        Process p;
        try {

            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            String output="";
            while ((line = reader.readLine())!= null) {
                output+=(line + "\n");
            }
            System.out.println(output);

        } catch (Exception e) {
            e.printStackTrace();
        }
        if(true)return;
        */


        //Not Sniper  Mode
		Launcher launcher = new Launcher();
		launcher.addInputResource("./source/act/");
        launcher.getEnvironment().setCommentEnabled(true);
        launcher.getEnvironment().setAutoImports(true);
		launcher.addProcessor((Processor) new NullDereferenceProcessor());
		launcher.run();
//	   	new SpoonModelTree(launcher.getFactory());

        /*
        //Sniper Mode . Add pavel's refDJPP branch as library of spoon to use this.
        Launcher launcher = new Launcher();
        launcher.addInputResource("/home/ashutosh/TBCH/act");
        launcher.getEnvironment().setCommentEnabled(true);
        launcher.getEnvironment().setAutoImports(true);
        launcher.buildModel();
        Factory f = launcher.getFactory();
        new SourceFragmentsTreeCreatingChangeCollector().attachTo(f.getEnvironment());

        CtClass<?> ctClass = launcher.getFactory().Class().get(ReferenceBuilder.class);
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