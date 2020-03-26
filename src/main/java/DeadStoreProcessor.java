import org.json.JSONArray;
import org.json.JSONException;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;

import java.util.HashSet;
import java.util.Set;


public class DeadStoreProcessor extends SonarWebAPIBasedProcessor<CtStatement> {

    String var;//contains name of variable which is uselessly assigned in the current bug.

    public DeadStoreProcessor(String projectKey) throws Exception {
        super(1854, projectKey);
    }

    @Override
    public boolean isToBeProcessed(CtStatement element)
    {
        if(element==null)
        {
            return false;
        }
        long line =-1;
        String targetName="",fileOfElement="";
        if(element instanceof CtLocalVariable)
        {
            targetName = ((CtLocalVariable)element).getSimpleName();
            line=(long) element.getPosition().getLine();
            String split1[]=element.getPosition().getFile().toString().split("/");
            fileOfElement=split1[split1.length-1];
        }
        else if(element instanceof CtAssignment)
        {
            targetName=((CtAssignment) element).getAssigned().toString();
            line=(long) element.getPosition().getLine();
            String split1[]=element.getPosition().getFile().toString().split("/");
            fileOfElement=split1[split1.length-1];
        }
        else return false;
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
                        var=targetName;
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
    public void process(CtStatement element) {
        System.out.println("BUG\n");
        CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();
        final String value = String.format("//[Spoon inserted check], repairs sonarqube rule 1854:Dead stores should be removed,\n//useless assignment to %s removed",
                var);
        snippet.setValue(value);
        element.delete();
    }
}