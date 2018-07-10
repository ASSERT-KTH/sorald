import org.json.JSONArray;
import org.json.JSONException;
import org.sonar.java.AnalyzerMessage;
import org.sonar.java.checks.DeadStoreCheck;
import org.sonar.java.checks.serialization.SerializableFieldInSerializableClassCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.ModifierKind;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SerializableFieldProcessor extends AbstractProcessor<CtField> {

    private JSONArray jsonArray;//array of JSONObjects, each of which is a bug
    private Set<Bug> SetOfBugs;//set of bugs, corresponding to jsonArray
    private Set<Long> SetOfLineNumbers;//set of line numbers corresponding to bugs, just for efficiency
    private Set<String> SetOfFileNames;//-----
    private Bug thisBug;               //current bug. This is set inside isToBeProcessed function
    private String thisBugName;        //name (message) of current thisBug.
    public SerializableFieldProcessor(List<File> files) throws Exception {
        Set<AnalyzerMessage> total = new HashSet<>();
        for(File file :files)
        {
            Set<AnalyzerMessage> verify = JavaCheckVerifier.verify(file.getAbsolutePath(), new SerializableFieldInSerializableClassCheck(), true);
            total.addAll(verify);
        }
        SetOfBugs = Bug.createSetOfBugs(total);
        SetOfLineNumbers=new HashSet<>();
        SetOfFileNames=new HashSet<>();
        thisBug=new Bug();
        for(Bug bug:SetOfBugs)
        {
            SetOfLineNumbers.add(bug.getLineNumber());
            SetOfFileNames.add(bug.getFileName());
        }
    }


    @Override
    public boolean isToBeProcessed(CtField element)
    {
        if(element==null)
        {
            return false;
        }
        long line =-1;
        String targetName="",fileOfElement="";
        line=(long) element.getPosition().getLine();
        fileOfElement=element.getPosition().getFile().getName();
        targetName=element.getSimpleName();
        if(!SetOfLineNumbers.contains(line)||!SetOfFileNames.contains(fileOfElement))
        {
            return false;
        }
        try {
            thisBug = new Bug();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for(Bug bug:SetOfBugs)
        {
            if(bug.getLineNumber()!=line||!bug.getFileName().equals(fileOfElement))
            {
                continue;
            }

            String bugName=bug.getName();
            String[] split = bugName.split("\"");
            for(String bugword:split)
            {
                if(targetName.equals(bugword))
                {
                    try
                    {
                        thisBug = new Bug(bug);
                        thisBugName = bugword;
                        return true;
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }
    @Override
    public void process(CtField element) {
        System.out.println("BUG");
        element.addModifier(ModifierKind.TRANSIENT);
    }
}