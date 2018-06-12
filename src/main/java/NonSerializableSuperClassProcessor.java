import org.json.JSONArray;
import org.json.JSONException;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.ModifierKind;

import java.util.HashSet;
import java.util.Set;

public class NonSerializableSuperClassProcessor extends AbstractProcessor<CtClass> {

    private JSONArray jsonArray;//array of JSONObjects, each of which is a bug
    private Set<Bug> SetOfBugs;//set of bugs, corresponding to jsonArray
    private Set<Long> SetOfLineNumbers;//set of line numbers corresponding to bugs, just for efficiency
    private Set<String> SetOfFileNames;//-----
    private Bug thisBug;               //current bug. This is set inside isToBeProcessed function
    private String thisBugName;        //name (message) of current thisBug.
    public NonSerializableSuperClassProcessor(String projectKey) throws Exception {
        jsonArray=ParseAPI.parse(2055,"",projectKey);
        SetOfBugs = Bug.createSetOfBugs(this.jsonArray);
        SetOfLineNumbers=new HashSet<Long>();
        SetOfFileNames=new HashSet<String>();
        thisBug=new Bug();
        for(Bug bug:SetOfBugs)
        {
            SetOfLineNumbers.add(bug.getLineNumber());
            SetOfFileNames.add(bug.getFileName());
        }
    }


    @Override
    public boolean isToBeProcessed(CtClass element)
    {
        if(element.getSuperclass()==null)
        {
            return false;
        }
        if(element==null)
        {
            return false;
        }
        long line =-1;
        String targetName="",fileOfElement="";

        line=(long) element.getPosition().getLine();
        String split1[]=element.getPosition().getFile().toString().split("/");
        fileOfElement=split1[split1.length-1];
        CtClass ct =(CtClass)element.getSuperclass().getTypeDeclaration();
        targetName=ct.getSimpleName();

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
    public void process(CtClass element) {

        String value = String.format("[Spoon inserted constructor], repairs sonarqube rule 2055:\nThe non-serializable super class of a \"Serializable\" class should have a no-argument constructor.\n");
        value=value+String.format("This class is a superclass of %s.",element.getSimpleName());
        CtComment comment = getFactory().createComment(value,CtComment.CommentType.BLOCK);

        CtClass ct =(CtClass)element.getSuperclass().getTypeDeclaration();
        CtConstructor alreadyPresent=ct.getConstructor();
        if(alreadyPresent!=null)
        {
            return;
        }
        CtConstructor constructor = getFactory().createConstructor();
        CtStatement statement = getFactory().createBlock();
        constructor.setBody(statement);
        constructor.setVisibility(ModifierKind.PUBLIC);
        constructor.addComment(comment);
        ct.addConstructor(constructor);
    }
}