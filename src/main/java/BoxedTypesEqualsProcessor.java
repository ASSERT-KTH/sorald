import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtExecutableReference;

import java.util.Arrays;

public class BoxedTypesEqualsProcessor extends AbstractProcessor<CtInvocation<?>> {

    public BoxedTypesEqualsProcessor(String projectKey) throws Exception {
        ParseAPI.parse(4973,"",projectKey);
    }

    @Override
    public boolean isToBeProcessed(CtInvocation<?> candidate)
    {
        if(candidate==null||candidate.getTarget()==null)
        {
            return false;
        }
        return false;
    }
    @Override
    public void process(CtInvocation<?> element) {
        
    }
}