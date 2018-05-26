import org.json.*;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;
import java.lang.reflect.Method;
import java.util.*;


public class NullDereferenceProcessor extends AbstractProcessor<CtInvocation<?>> {

    private JSONArray jsonArray;
    private Set<Bug> SetOfBugs;
    private Set<Long> SetOfLineNumbers;
    private Set<String> SetOfFileNames;
    private Bug thisBug;
    public  NullDereferenceProcessor() throws Exception {
        throw new Exception("ERROR : Please pass JsonArray to constructor of this processor");
    }

    public NullDereferenceProcessor(JSONArray jsonArray) throws Exception {
        this.jsonArray=jsonArray;
        SetOfBugs = Bug.createSetOfBugs(this.jsonArray);
        SetOfLineNumbers=new HashSet<Long>();
        SetOfFileNames=new HashSet<String>();
        thisBug=new Bug();
        for(Bug bug:SetOfBugs)
        {
            SetOfLineNumbers.add(bug.getLineNumber());
            SetOfFileNames.add(bug.getFileName());
            thisBug=bug;
        }
    }


        @Override
        public boolean isToBeProcessed(CtInvocation<?> element)
        {
            if(element==null||element.getTarget()==null)
            {
                return false;
            }

            CtExpression expr=element.getTarget();
            long line = (long) expr.getPosition().getLine();
            String targetName=expr.toString();
            String split1[]=element.getPosition().getFile().toString().split("/");
            String fileOfElement=split1[split1.length-1];
            if(!SetOfLineNumbers.contains(line)||!SetOfFileNames.contains(fileOfElement))
            {
                return false;
            }
            boolean contains=false;
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
                    if(targetName.contains(bugword))
                    {
                        try {
                            thisBug=new Bug(bug);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        contains=true;
                        break;
                    }
                }
                if(contains)
                {
                    break;
                }
            }
            if(!contains)
            {
                return false;
            }
            return true;

        }
    @Override
    public void process(CtInvocation<?> element) {

        System.out.println(element+"    hello     "+element.getTarget()+"      hello     "+element.getPosition());
        System.out.println("\n\n");

        CtExpression expression=element.getTarget();

        boolean isVar=expression instanceof CtVariableRead;
        System.out.println(isVar);


        CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();
        final String value = String.format("if (%s == null) "
                        + "throw new IllegalArgumentException(\"[Spoon inserted check] null passed as parameter\");",
                element.getSimpleName());
        snippet.setValue(value);

        // we insert the snippet at the beginning of the method body.
        if (isVar) {
            element.getParent(CtStatement.class).insertBefore(snippet);
        }

    }
}