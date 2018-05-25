import org.json.JSONArray;
import spoon.Launcher;
import spoon.processing.Processor;

public class My {
	public static void main(String[] args) throws Exception
    {
        JSONArray jsonArray=ParseAPI.parse(2259,"src/main/java/spoon/MavenLauncher.java");
		Launcher launcher = new Launcher();
		launcher.addInputResource("/home/ashutosh/TBCH/act/");
		launcher.addProcessor((Processor) new NullDereferenceProcessor(jsonArray));
		launcher.run();
//		new SpoonModelTree(launcher.getFactory());
		System.out.println("done");
	}
}