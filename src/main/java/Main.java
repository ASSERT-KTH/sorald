import spoon.Launcher;
import spoon.processing.Processor;
import spoon.support.gui.SpoonModelTree;


public class Main {
	public static void main(String[] args) throws Exception
    {
//        JSONArray jsonArray=ParseAPI.parse(2259,"");//moved the API call to the specific processor
//        JSONArray jsonArray=ParseAPI.parse(2259,"src/main/java/spoon/MavenLauncher.java");

        //Not Sniper  Mode
		Launcher launcher = new Launcher();
		launcher.addInputResource("/home/ashutosh/TBCH/act/");
//		launcher.addInputResource("/home/ashutosh/TBCH/act/src/main/java/spoon/support/StandardEnvironment.java");
//		launcher.addProcessor((Processor) new NullDereferenceProcessor());
		launcher.addProcessor((Processor) new SerializableFieldProcessor());
		launcher.run();
//	   	new SpoonModelTree(launcher.getFactory());

        /*
        Launcher launcher = new Launcher();
        launcher.addInputResource("/home/ashutosh/TBCH/act");
        launcher.getEnvironment().setCommentEnabled(true);
        launcher.getEnvironment().setAutoImports(true);
        launcher.buildModel();
        Factory f = launcher.getFactory();



        new SourceFragmentsTreeCreatingChangeCollector().attachTo(f.getEnvironment());
        CtClass<?> ctClass = launcher.getFactory().Class().get(ReferenceBuilder.class);

//        SniperHelper.process(ctClass);

//        ctClass.getField("string").setSimpleName("modified");
        System.out.println(ctClass.getSimpleName());

        ChangesAwareDefaultJavaPrettyPrinter printer = new ChangesAwareDefaultJavaPrettyPrinter(f.getEnvironment());
        CompilationUnit cu = f.CompilationUnit().getOrCreate(ctClass);
        List<CtType<?>> toBePrinted = new ArrayList<>();
        toBePrinted.add(ctClass);
//        ctClass.toString();
//        System.out.println(ctClass.toString());

        printer.calculate(cu, toBePrinted);
        */


        System.out.println("done");
	}
}