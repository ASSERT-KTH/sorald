package sorald.processor;

import sorald.Constants;
import sorald.annotations.IncompleteProcessor;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtMethod;

@IncompleteProcessor(description = "does not fix null returning clone()")
@ProcessorAnnotation(
        key = 2225,
        description = "\"toString()\" and \"clone()\" methods should not return null")
public class ToStringReturningNullProcessor extends SoraldAbstractProcessor<CtReturn<?>> {

    @Override
    protected boolean canRepairInternal(CtReturn<?> candidate) {
        CtMethod<?> parentMethod = candidate.getParent(CtMethod.class);
        return parentMethod.getSignature().equals(Constants.TOSTRING_METHOD_NAME + "()")
                && candidate.getReturnedExpression().toString().equals("null");
    }

    @Override
    protected void repairInternal(CtReturn<?> element) {
        element.getReturnedExpression().replace(getFactory().Code().createLiteral(""));
    }
}
