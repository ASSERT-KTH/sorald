import org.json.JSONArray;
import org.json.JSONException;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtExecutableReference;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.lang.Math.abs;


public class ArrayToStringProcessor extends AbstractProcessor<CtInvocation<?>> {

    private JSONArray jsonArray;//array of JSONObjects, each of which is a bug
    private Set<Bug> SetOfBugs;//set of bugs, corresponding to jsonArray
    private Set<Long> SetOfLineNumbers;//set of line numbers corresponding to bugs, just for efficiency
    private Set<String> SetOfFileNames;//-----
    private Bug thisBug;               //current bug. This is set inside isToBeProcessed function
    private String thisBugName;        //name (message) of current thisBug.

    public ArrayToStringProcessor(String projectKey) throws Exception {
        jsonArray= ParseAPI.parse(2116,"",projectKey);
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
    public boolean isToBeProcessed(CtInvocation<?> candidate)
    {
        if(candidate==null||candidate.getTarget()==null)
        {
            return false;
        }
        if(candidate.getTarget().getType().isArray()){
            if(candidate.getExecutable().toString().equals("toString()")){
                return true;
            }
        }
        return false;
    }
    @Override
    public void process(CtInvocation<?> element) {
        CtExpression prevTarget = element.getTarget();
        CtCodeSnippetExpression newTarget = getFactory().Code().createCodeSnippetExpression("Arrays");
        CtType arraysClass = getFactory().Class().get(Arrays.class);
        CtMethod toStringMethod = (CtMethod) arraysClass.getMethodsByName("toString").get(0);
        CtExecutableReference refToString = getFactory().Executable().createReference(toStringMethod);
        CtInvocation arraysToString = getFactory().Code().createInvocation(newTarget, refToString, prevTarget);

        arraysToString.setImplicit(false);

        // CtCodeSnippetStatement snippetExpression2 = getFactory().Code().createCodeSnippetStatement("Arrays2");
        // CtElement target2 = element.getExecutable();
        element.replace(arraysToString);
        // target2.replace(snippetExpression2);
        // element.delete();
    }
}