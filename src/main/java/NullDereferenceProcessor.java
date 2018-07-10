import org.json.JSONArray;
import org.json.JSONException;
import org.sonar.java.AnalyzerMessage;
import org.sonar.java.checks.DeadStoreCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import org.sonar.java.se.checks.NullDereferenceCheck;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.cu.SourcePosition;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.Math.abs;


public class NullDereferenceProcessor extends AbstractProcessor<CtInvocation<?>> {

    private JSONArray jsonArray;//array of JSONObjects, each of which is a bug
    private Set<Bug> SetOfBugs;//set of bugs, corresponding to jsonArray
    private Set<Long> SetOfLineNumbers;//set of line numbers corresponding to bugs, just for efficiency
    private Set<String> SetOfFileNames;//-----
    private Bug thisBug;               //current bug. This is set inside isToBeProcessed function
    private String thisBugName;        //name (message) of current thisBug.

    public NullDereferenceProcessor(List<File> files) throws Exception {
        Set<AnalyzerMessage> total = new HashSet<>();
        for(File file :files)
        {
            Set<AnalyzerMessage> verify = JavaCheckVerifier.verify(file.getAbsolutePath(), new NullDereferenceCheck(), true);
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
        public boolean isToBeProcessed(CtInvocation<?> element)
        {
            if(element==null||element.getTarget()==null)
            {
                return false;
            }
            CtExpression expr=element.getTarget();
            long line = (long) element.getPosition().getLine();
            String targetName=expr.toString();
            String fileOfElement=element.getPosition().getFile().getName();
            if(!SetOfLineNumbers.contains(line)||!SetOfFileNames.contains(fileOfElement))
            {
                return false;
            }
            try {
                thisBug = new    Bug();
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
                    if(bugword.indexOf('(')!=-1)
                    {
                        bugword = bugword.substring(0, bugword.indexOf('('));
                    }
                    if(targetName.contains(bugword))
                    {
                        try {
                            SourcePosition sp = expr.getPosition();
                            if (!sp.isValidPosition()) {
                                continue;
                            }
                            int exprcolumn = sp.getColumn();
                            int bugcolumn = bug.getMessage().primaryLocation().startCharacter;
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
        System.out.println("BUG\n");
        CtExpression target=element.getTarget();
        CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();
        final String value = String.format("if (%s == null) "
                        + "throw new IllegalStateException(\"[Spoon inserted check], %s might be null\");",
                target.toString(),thisBugName);
        snippet.setValue(value);
        if (target instanceof CtVariableRead) {
            CtStatement st= element.getParent(CtStatement.class);
            if(st!=null) {
                st.insertBefore(snippet);
            }
        }
        else if(target instanceof CtInvocation)
        {
            CtTry ctTry = getFactory().createTry();
            CtBlock bb = getFactory().createCtBlock(ctTry);
            CtBlock ctBlock1 = element.getParent(CtBlock.class);
            ctBlock1.replace(bb);
            ctTry.setBody(ctBlock1);
            CtCodeSnippetStatement snipcat = getFactory().createCodeSnippetStatement();
            final String cat = "catch(Exception e)\n" +
                    "{\n" +
                    "    throw new IllegalSt;\n" +
                    "}";
            snipcat.setValue(cat);
            ctTry.insertAfter(snipcat);
        }
    }
}