import org.json.JSONArray;
import spoon.Launcher;
import spoon.processing.Processor;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.lang.System.exit;
import static java.lang.Thread.sleep;

public class TestHelp
{

    public static Map<Integer, Class<? extends Processor>> rule;

    public static void initmap()
    {
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

    public static boolean checkBugs(String pathToFile,int rulekey) throws Exception {
        String cdrep = "./src/test/sonarepaired/";
        String cdtest = "./src/test/sonatest/";

        String arr[]=pathToFile.split("/");
        String fileName=arr[arr.length-1];

        copy("./spooned/"+fileName,cdrep+pathToFile);

        Execute.command("mvn clean package",cdrep,true);

        doSonarAnalysis(cdrep);

        sleep(1000*10);

        JSONArray array = ParseAPI.parse(rulekey,"","se.kth:sonarepaired");

        copy(cdtest+pathToFile,cdrep+pathToFile);//copy file back from sonatest to sonarepair

//        Execute.command("rm -rf ./spooned/");

        if(array.length()==0)
        {
            return false;
        }
        else
        {
            Set<Bug> setOfBugs = Bug.createSetOfBugs(array);
            for(Bug bug : setOfBugs)
            {
                String bugFileName = bug.getFileName();
                if(bugFileName.equals(fileName))
                {
                    return true;
                }
            }
            return false;
        }
//        return (array.length()>0);
    }

    public static void copy(String from,String to)
    {
        Execute.command("cp " + from + " " + to);
    }

    public static void doSonarAnalysis(String dir)
    {
        String sonarAnalysis = "mvn sonar:sonar   -Dsonar.organization=ashutosh1598-github   -Dsonar.host.url=https://sonarcloud.io   -Dsonar.login=2b8cc80c8434c1ea108801afa994a6a91d4facd7";
        Execute.command(sonarAnalysis,dir,true);
    }

    public static boolean hasSonarBug(String pathToFile,int rulekey) throws Exception {
        return checkBugs(pathToFile,rulekey);
    }
}
