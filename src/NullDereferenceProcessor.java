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


public class NullDereferenceProcessor extends AbstractProcessor<CtExecutableReference<?>> {

    private JSONArray jsonArray;
    private Set<Bug> SetOfBugs;
    private Set<Long> SetOfLineNumbers;
    public  NullDereferenceProcessor() throws Exception {
        throw new Exception("ERROR : Please pass JsonArray to constructor of this processor");
    }

    public NullDereferenceProcessor(JSONArray jsonArray) throws Exception {
        this.jsonArray=jsonArray;
        SetOfBugs = Bug.createSetOfBugs(this.jsonArray);
        SetOfLineNumbers=new HashSet<Long>();
        for(Bug bug:SetOfBugs)
        {
            SetOfLineNumbers.add(bug.getLineNumber());
        }
    }


        @Override
        public boolean isToBeProcessed(CtExecutableReference<?> element)
        {
            return true;
            /*
            boolean answer = !element.getType().isPrimitive();// only for objects
            return answer;
            */

        }
    @Override
    public void process(CtExecutableReference<?> element) {
        if(element==null)
        {
            return;
        }

        CtElement executable=element.getParent();
        long line = (long) executable.getPosition().getLine();
//        String signature = element.getSignature();
        if(true)
        {

        }
        String executableName=executable.toString();
        boolean contains=false;
        Bug thisBug= null;
        try {
            thisBug = new Bug();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for(Bug bug:SetOfBugs)
        {
            if(bug.getLineNumber()!=line)
            {
                continue;
            }
            String bugName=bug.getName();
            String[] split = bugName.split("\"");
            for(String bugword:split)
            {
                if(executableName.contains(bugword))
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
            return;
        }
        System.out.println(thisBug.getName()+" hello");
//        System.out.println(signature);
        System.out.println(element+"    hello     "+element.getParent()+"      hello     "+element.getParent().getPosition()+ "  "+element.getPath());
        System.out.println("\n\n");
        /*
        CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();
        // this snippet contains an if check.
        final String value = String.format("if (%s == null) "
                        + "throw new IllegalArgumentException(\"[Spoon inserted check] null passed as parameter\");",
                element.getSimpleName());
        snippet.setValue(value);

        // we insert the snippet at the beginning of the method body.
        if (element.getParent(CtExecutable.class).getBody() != null) {
            element.getParent(CtExecutable.class).getBody().insertBegin(snippet);
        }
        */
    }
}