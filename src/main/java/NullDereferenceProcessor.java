import org.json.JSONArray;
import org.json.JSONException;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtExecutableReference;

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
                        try {
                            SourcePosition sp = expr.getPosition();
                            if (!sp.isValidPosition()) {
                                continue;
                            }
                            int exprcolumn = sp.getColumn();
                            int bugcolumn = bug.getJsonObject().getJSONObject("textRange").getInt("startOffset");
                            if (element.getTarget() instanceof CtVariableRead) {
                                if(targetName.equals(bugword))
                                {
                                    if (abs(exprcolumn - bugcolumn) <= 1) {
                                        thisBug = new Bug(bug);
                                        thisBugName = bugword;
                                        return true;
                                    }
                                }

                            } else if(element.getTarget() instanceof CtInvocation) {
                                CtInvocation target1 = (CtInvocation) element.getTarget();
                                if(target1.getExecutable().getSimpleName().equals(bugword)) {
                                    if (abs(exprcolumn - bugcolumn) <= 1) {
                                        thisBug = new Bug(bug);
                                        thisBugName = bugword;
                                        return true;
                                    }
                                }
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
        System.out.println(element +"  dfgdfg"+element.getPosition());
        CtExpression target=element.getTarget();
        CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();
        final String value = String.format("if (%s == null) "
                        + "throw new IllegalStateException(\"[Spoon inserted check], %s might be null\");",
                target.toString(),thisBugName);
        snippet.setValue(value);
        String comm = String.format("Repairs sonarqube rule 2259:Null pointers should not be dereferenced\n%s might be null",thisBugName);
        CtComment comment = getFactory().createComment(comm,CtComment.CommentType.BLOCK);
        if (target instanceof CtVariableRead) {
            snippet.addComment(comment);
            CtStatement st= element.getParent(CtStatement.class);
//            System.out.println(st.toString());
            if(st!=null) {
//                st.insertBefore(snippet);
            }
        }
        else if(target instanceof CtInvocation)
        {
            CtTry ctTry = getFactory().createTry();
//            CtBlock ctBlock = element.getParent(CtBlock.class).clone();
//            ctTry.setBody(ctBlock);
//            CtElement elem = (CtElement) ctTry;
            CtBlock bb = getFactory().createCtBlock(ctTry);
            CtBlock ctBlock1 = element.getParent(CtBlock.class);
            ctBlock1.replace(bb);
            ctTry.setBody(ctBlock1);
            ctTry.addComment(comment);
            CtCodeSnippetStatement snipcat = getFactory().createCodeSnippetStatement();
            final String cat = "catch(Exception e)\n" +
                    "{\n" +
                    "    e.printStackTrace();\n" +
                    "}";
            snipcat.setValue(cat);
            ctTry.insertAfter(snipcat);
        }
        System.out.println("Bug\n");
    }
}