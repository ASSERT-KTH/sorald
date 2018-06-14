import org.json.JSONArray;
import org.json.JSONException;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtVariableReference;

import java.util.HashSet;
import java.util.Set;

public class ResourceCloseProcessor extends AbstractProcessor<CtConstructorCall> {

    private JSONArray jsonArray;//array of JSONObjects, each of which is a bug
    private Set<Bug> SetOfBugs;//set of bugs, corresponding to jsonArray
    private Set<Long> SetOfLineNumbers;//set of line numbers corresponding to bugs, just for efficiency
    private Set<String> SetOfFileNames;//-----
    private Bug thisBug;               //current bug. This is set inside isToBeProcessed function
    private String thisBugName;        //name (message) of current thisBug.
    String var;//contains name of resource which is unclosed in the current bug.

    public ResourceCloseProcessor(String projectKey) throws Exception {
        jsonArray= ParseAPI.parse(2095,"",projectKey);
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
    public boolean isToBeProcessed(CtConstructorCall element)
    {
        if(element==null)
        {
            return false;
        }

        long line =-1;
        String targetName="",fileOfElement="";
        targetName = element.getExecutable().getDeclaringType().getSimpleName();
        line=(long) element.getPosition().getLine();
        String split1[]=element.getPosition().getFile().toString().split("/");
        fileOfElement=split1[split1.length-1];



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
    public void process(CtConstructorCall element) {

        CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();
        final String value = String.format("[Spoon inserted try-with-resource],\n Repairs sonarqube rule 2095:\n %s should be closed",var);
        snippet.setValue(value);
        CtComment comment = getFactory().createComment(value, CtComment.CommentType.BLOCK);

        CtElement parent = element.getParent(e -> e instanceof CtAssignment || e instanceof CtLocalVariable);

        if(parent instanceof CtLocalVariable)
        {
            CtLocalVariable variable = ((CtLocalVariable) parent).clone();

            CtBlock block=parent.getParent(CtBlock.class);
            parent.delete();

            CtTryWithResource tryWithResource = getFactory().createTryWithResource();
            tryWithResource.addResource(variable);
            tryWithResource.addComment(comment);
            CtBlock bb = getFactory().createCtBlock(tryWithResource);
            block.replace(bb);
            tryWithResource.setBody(block);
        }
        else if(parent instanceof CtAssignment)
        {
            CtAssignment assign= (CtAssignment) parent;
            CtExpression expr = assign.getAssigned();

            if(expr instanceof CtVariableWrite)
            {
                CtVariableWrite variableWrite = (CtVariableWrite) expr;
                CtVariableReference variableReference = variableWrite.getVariable();
                if(variableReference.getDeclaration() instanceof CtLocalVariable)
                {
                    CtLocalVariable var = (CtLocalVariable) variableReference.getDeclaration();
                    CtLocalVariable variable = var.clone();
                    variable.setAssignment(assign.getAssignment().clone());
                    CtBlock block=parent.getParent(CtBlock.class);

                    parent.delete();
                    var.delete();

                    CtTryWithResource tryWithResource = getFactory().createTryWithResource();
                    tryWithResource.addResource(variable);
                    tryWithResource.addComment(comment);

                    CtBlock bb= getFactory().createCtBlock(tryWithResource);
                    block.replace(bb);
                    tryWithResource.setBody(block);
                }
            }
        }
    }
}