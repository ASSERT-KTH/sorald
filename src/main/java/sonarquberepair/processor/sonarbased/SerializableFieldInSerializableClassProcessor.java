package sonarquberepair.processor.sonarbased;

import org.json.JSONArray;
import org.json.JSONException;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtComment;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.ModifierKind;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SerializableFieldInSerializableClassProcessor extends SonarWebAPIBasedProcessor<CtField> {

    public SerializableFieldInSerializableClassProcessor(String projectKey) throws Exception {
        super(1948, projectKey);
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
        String split1[]=element.getPosition().getFile().toString().split("/");
        fileOfElement=split1[split1.length-1];
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
        element.addModifier(ModifierKind.TRANSIENT);
        List<CtComment> comments = element.getComments();
        CtComment sp = null;
        for(CtComment comment : comments)
        {
            if(comment.getContent().indexOf("Noncompliant")!=-1)
            {
                sp = comment;
            }
        }
        if(sp!=null)
        {
            element.removeComment(sp);
        }
    }
}