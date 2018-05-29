import org.json.JSONArray;
import spoon.Launcher;
import spoon.processing.Processor;
import spoon.support.gui.SpoonModelTree;

public class Main {
	public static void main(String[] args) throws Exception
    {
        JSONArray jsonArray=ParseAPI.parse(2259,"");
//        JSONArray jsonArray=ParseAPI.parse(2259,"src/main/java/spoon/MavenLauncher.java");


        //Not Sniper  Mode
		Launcher launcher = new Launcher();
		launcher.addInputResource("/home/ashutosh/TBCH/act/");
//		launcher.addInputResource("/home/ashutosh/TBCH/act/src/main/java/spoon/support/StandardEnvironment.java");
		launcher.addProcessor((Processor) new NullDereferenceProcessor(jsonArray));
		launcher.run();
//		new SpoonModelTree(launcher.getFactory());
//

		/*
		Launcher launcher = new Launcher();
		launcher.addInputResource("/home/ashutosh/TBCH/act/");
		launcher.getEnvironment().setCommentEnabled(true);
		launcher.getEnvironment().setAutoImports(true);
		launcher.buildModel();
//		launcher.addProcessor((Processor) new NullDereferenceProcessor(jsonArray));

		Factory f = launcher.getFactory();
		new SourceFragmentsTreeCreatingChangeCollector().attachTo(f.getEnvironment());

		/*
		launcher.process();
		launcher.getEnvironment().setDefaultFileGenerator(new JavaOutputProcessor(new ChangesAwareDefaultJavaPrettyPrinter(f.getEnvironment())));
		launcher.prettyprint();
		*/


		/*
		ChangesAwareDefaultJavaPrettyPrinter printer = new ChangesAwareDefaultJavaPrettyPrinter(f.getEnvironment());
		CompilationUnit cu = f.CompilationUnit().getOrCreate(ctClass);
		List<CtType<?>> toBePrinted = new ArrayList<>();
		toBePrinted.add(ctClass);
		printer.calculate(cu, toBePrinted);
		*/


		System.out.println("done");
	}
}