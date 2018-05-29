import org.json.JSONArray;
import spoon.Launcher;
import spoon.MavenLauncher;
import spoon.experimental.modelobs.SourceFragmentsTreeCreatingChangeCollector;
import spoon.processing.Processor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.cu.CompilationUnit;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.printer.change.ChangesAwareDefaultJavaPrettyPrinter;
import spoon.support.gui.SpoonModelTree;

import java.util.ArrayList;
import java.util.List;


public class Main {
	public static void main(String[] args) throws Exception
    {
//        JSONArray jsonArray=ParseAPI.parse(2259,"");
//        JSONArray jsonArray=ParseAPI.parse(2259,"src/main/java/spoon/MavenLauncher.java");

		/*
        //Not Sniper  Mode
		Launcher launcher = new Launcher();
		launcher.addInputResource("/home/ashutosh/TBCH/act/");
//		launcher.addInputResource("/home/ashutosh/TBCH/act/src/main/java/spoon/support/StandardEnvironment.java");
		launcher.addProcessor((Processor) new NullDereferenceProcessor(jsonArray));
		launcher.run();
//	   	new SpoonModelTree(launcher.getFactory());
        */
        Launcher launcher = new Launcher();
        launcher.addInputResource("/home/ashutosh/TBCH/act");
        launcher.getEnvironment().setCommentEnabled(true);
        launcher.getEnvironment().setAutoImports(true);
        launcher.buildModel();
        Factory f = launcher.getFactory();



        new SourceFragmentsTreeCreatingChangeCollector().attachTo(f.getEnvironment());
        CtClass<?> ctClass = launcher.getFactory().Class().get(A.class);

//        SniperHelper.process(ctClass,jsonArray);

//        ctClass.getField("string").setSimpleName("modified");
        System.out.println(ctClass.getSimpleName());
        ChangesAwareDefaultJavaPrettyPrinter printer = new ChangesAwareDefaultJavaPrettyPrinter(f.getEnvironment());
        CompilationUnit cu = f.CompilationUnit().getOrCreate(ctClass);
        List<CtType<?>> toBePrinted = new ArrayList<>();
        toBePrinted.add(ctClass);

        printer.calculate(cu, toBePrinted);



        System.out.println("done");
	}
}