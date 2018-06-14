import org.json.JSONArray;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import spoon.Launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;


class DeadStoreProcessorTest {

    /**
     *
     * @param command command to run on terminal
     */
    private static boolean suppress=false;
    private void executeCommand(String command,String dir)
    {
        System.out.println("command : cd "+dir+" && "+command);
        StringBuffer output = new StringBuffer();
        Process p;
        try {
            p = Runtime.getRuntime().exec(command,null,new File(dir));
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if(!suppress)
        {
            System.out.println(output.toString());
        }
    }

    @Test
    void process()throws Exception
    {
        executeCommand("pwd",".");

        //Not Sniper  Mode
        Launcher launcher = new Launcher();
//        launcher.addInputResource("./src/test/sonatest/src/main/java/DeadStores.java");
        String pathToFile = "src/main/java/DeadStores.java";
        launcher.addInputResource("./src/test/sonatest/"+pathToFile);
        launcher.getEnvironment().setCommentEnabled(true);
        launcher.getEnvironment().setAutoImports(true);
        launcher.getEnvironment().setTabulationSize(4);
        launcher.getEnvironment().useTabulations(true);
        String projectKey="se.kth:sonatest";
        launcher.addProcessor(new DeadStoreProcessor(projectKey));
        launcher.run();
        String sonarAnalysis = "mvn sonar:sonar   -Dsonar.organization=ashutosh1598-github   -Dsonar.host.url=https://sonarcloud.io   -Dsonar.login=2b8cc80c8434c1ea108801afa994a6a91d4facd7";
        String cdrep = "./src/test/sonarepaired/";
        String cdtest = "./src/test/sonatest/";
        executeCommand("cp "+"./spooned/DeadStores.java" +" "+cdrep+pathToFile,".");
        suppress=true;
        executeCommand("mvn clean package",cdrep);
        executeCommand(sonarAnalysis,cdrep);
        suppress=false;
        JSONArray array = ParseAPI.parse(1854,"","se.kth:sonarepaired");
        Assertions.assertTrue(array.length()==0);
    }
}