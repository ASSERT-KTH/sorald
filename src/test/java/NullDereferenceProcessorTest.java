import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import spoon.Launcher;
import spoon.processing.Processor;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.*;

class NullDereferenceProcessorTest {

    @BeforeAll
    public static void init()throws Exception
    {

        String command = "rm -rf ./spooned/";
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

        //Not Sniper  Mode
        Launcher launcher = new Launcher();
        launcher.addInputResource("./source/act/");
        launcher.getEnvironment().setCommentEnabled(true);
        launcher.getEnvironment().setAutoImports(true);
        launcher.addProcessor((Processor) new NullDereferenceProcessor());
        launcher.run();
//	   	new SpoonModelTree(launcher.getFactory());



    }

    @Test
    void process()throws Exception{

        //Not Sniper  Mode
        Launcher launcher = new Launcher();
        launcher.addInputResource("./source/act/");
        launcher.getEnvironment().setCommentEnabled(true);
        launcher.getEnvironment().setAutoImports(true);
        launcher.addProcessor((Processor) new NullDereferenceProcessor());
        launcher.run();
//	   	new SpoonModelTree(launcher.getFactory());



    }
}