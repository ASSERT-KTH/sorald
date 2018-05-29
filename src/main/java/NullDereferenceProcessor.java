import org.json.JSONArray;
import org.json.JSONException;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtExecutableReference;

import java.util.HashSet;
import java.util.Set;


public class NullDereferenceProcessor extends AbstractProcessor<CtInvocation<?>> {

    private JSONArray jsonArray;
    private Set<Bug> SetOfBugs;
    private Set<Long> SetOfLineNumbers;
    private Set<String> SetOfFileNames;
    private Bug thisBug;
    private String thisBugName;
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
//            System.out.println(bug.getJsonObject().toString()+"\n\n");
        }
    }


        @Override
        public boolean isToBeProcessed(CtInvocation<?> element)
        {
            if(element==null||element.getTarget()==null)
            {
                return false;
            }
//            System.out.println(element+"    hello     "+element.getTarget()+"      hello     "+element.getPosition());
//            System.out.println("\n\n");

            CtExpression expr=element.getTarget();

            long line = (long) element.getPosition().getLine();

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
                            thisBugName=bugword;
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
//        System.out.println(element+"    hello     "+element.getTarget()+"      hello     "+element.getPosition() +"   hello  "+element.getTarget().getType());
        System.out.println(element+"    hello     "+element.getTarget()+"      hello     "+element.getPosition());




        CtExpression target=element.getTarget();

        CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();
        final String value = String.format("if (%s == null) "
                        + "throw new IllegalStateException(\"[Spoon inserted check], "+"%s might be null\");",
                target.toString(),thisBugName);
        snippet.setValue(value);

//        boolean isVariable=target instanceof CtVariableRead;
        if (target instanceof CtVariableRead) {
            element.getParent(CtStatement.class).insertBefore(snippet);
        }
        else if(target instanceof CtInvocation)
        {

            CtTry ctTry= getFactory().createTry();
            CtBlock ctBlock=element.getParent(CtBlock.class).clone();
            ctTry.setBody(ctBlock);
            CtElement elem=(CtElement) ctTry;
            CtBlock ctBlock1=element.getParent(CtBlock.class);
            ctBlock1.replace(elem);
            CtCodeSnippetStatement snipcat= getFactory().createCodeSnippetStatement();
            final String cat="catch(Exception e)\n" +
                    "{\n" +
                    "    e.printStackTrace();\n" +
                    "}";
            snipcat.setValue(cat);
            ctTry.insertAfter(snipcat);

//            thisBug.printBugLocations();
            CtInvocation invo=(CtInvocation) target;
            System.out.println();
        }

        System.out.println("\n\n");
    }
}