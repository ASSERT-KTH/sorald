import org.json.JSONArray;
import org.json.JSONException;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;

import java.util.HashSet;
import java.util.Set;

import static java.lang.Math.abs;


public class NullDereferenceProcessor extends AbstractProcessor<CtInvocation<?>> {

    private JSONArray jsonArray;//array of JSONObjects, each of which is a bug
    private Set<Bug> SetOfBugs;//set of bugs, corresponding to jsonArray
    private Set<Long> SetOfLineNumbers;//set of line numbers corresponding to bugs, just for efficiency
    private Set<String> SetOfFileNames;//-----
    private Bug thisBug;               //current bug. This is set inside isToBeProcessed function
    private String thisBugName;        //name (message) of current thisBug.

    public NullDereferenceProcessor() throws Exception {
        jsonArray=ParseAPI.parse(2259,"");
//        JSONArray jsonArray=ParseAPI.parse(2259,"src/main/java/spoon/MavenLauncher.java");
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
        public boolean isToBeProcessed(CtInvocation<?> element)
        {
            if(element==null||element.getTarget()==null)
            {
                return false;
            }

            CtExpression expr=element.getTarget();
            long line = (long) element.getPosition().getLine();
            String targetName=expr.toString();
            String split1[]=element.getPosition().getFile().toString().split("/");
            String fileOfElement=split1[split1.length-1];
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
                    if(targetName.contains(bugword))
                    {
                        try
                        {
                            SourcePosition sp = expr.getPosition();
                            int exprcolumn = sp.getColumn();
                            int bugcolumn = bug.locations.getJSONObject(0).getJSONObject("textRange").getInt("startOffset");
                            if(abs(exprcolumn-bugcolumn)<=1) {
                                thisBug = new Bug(bug);
                                thisBugName = bugword;
                                return true;
                            }
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
    public void process(CtInvocation<?> element) {

        CtExpression target=element.getTarget();

        CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();
        final String value = String.format("if (%s == null) "
                        + "throw new IllegalStateException(\"[Spoon inserted check], repairs sonarqube rule 2259:Null pointers should not be dereferenced,\n"+"%s might be null\");",
                target.toString(),thisBugName);
        snippet.setValue(value);

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
        }

    }
}