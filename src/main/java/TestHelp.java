import org.json.JSONArray;
import spoon.Launcher;
import spoon.processing.Processor;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.exit;

public class TestHelp
{

    public static Map<Integer, Class<? extends Processor>> rule;

    public static void initmap()
    {
        if(rule==null)
        rule = new HashMap<>();
        rule.putIfAbsent(1854,DeadStoreProcessor.class);
        rule.putIfAbsent(1948,SerializableFieldProcessor.class);
        rule.putIfAbsent(2055,NonSerializableSuperClassProcessor.class);
        rule.putIfAbsent(2095,ResourceCloseProcessor.class);
        rule.putIfAbsent(2259,NullDereferenceProcessor.class);
    }
    public  static  Class<?> getProcessor(int ruleKey)
    {
        if(rule==null)
        {
            initmap();
        }
        if(!rule.containsKey(ruleKey))
        {
            System.out.println("Sorry. Repair not available for rule "+ruleKey);
            exit(0);
        }
        return rule.get(ruleKey);
    }

    public static void repair(String pathToFile,int rulekey) throws Exception {

        initmap();
        //Not Sniper  Mode
        Launcher launcher = new Launcher();

        launcher.addInputResource("./src/test/sonatest/"+pathToFile);

        launcher.getEnvironment().setCommentEnabled(true);
        launcher.getEnvironment().setAutoImports(true);
        launcher.getEnvironment().setTabulationSize(4);
        launcher.getEnvironment().useTabulations(true);

        String projectKey="se.kth:sonatest";

        Class<?> processor = getProcessor(rulekey);
        Constructor<?> cons = processor.getConstructor(String.class);
        Object object = cons.newInstance(projectKey);
        launcher.addProcessor( (Processor)object);
        launcher.run();
    }

    public static boolean runAnalysis(String pathToFile,int rulekey) throws Exception {
        String sonarAnalysis = "mvn sonar:sonar   -Dsonar.organization=ashutosh1598-github   -Dsonar.host.url=https://sonarcloud.io   -Dsonar.login=2b8cc80c8434c1ea108801afa994a6a91d4facd7";
        String cdrep = "./src/test/sonarepaired/";
        String cdtest = "./src/test/sonatest/";

        String arr[]=pathToFile.split("/");
        String fileName=arr[arr.length-1];

        Execute.command("cp "+"./spooned/" + fileName +" "+cdrep+pathToFile,".");
        Execute.command("mvn clean package",cdrep,true);
        Execute.command(sonarAnalysis,cdrep,true);

        JSONArray array = ParseAPI.parse(rulekey,"","se.kth:sonarepaired");

        Execute.command("cp "+cdtest+pathToFile+" "+cdrep+pathToFile);//copy file back from sonatest to sonarepair
        Execute.command("rm -rf ./spooned/");


        return (array.length()>0);
    }

    public static boolean hasSonarBug(String pathToFile,int rulekey) throws Exception {
        return runAnalysis(pathToFile,rulekey);
    }
}
