package sorald.processor;

import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtSynchronized;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

@ProcessorAnnotation(
        key = 3067,
        description = "\"getClass\" should not be used for synchronization")
public class SynchronizationOnGetClassProcessor extends SoraldAbstractProcessor<CtSynchronized> {

    @Override
    protected void repairInternal(CtSynchronized element) {
        CtExpression<?> expression = element.getExpression();
        CtTypeReference<?> typeRef;
        if (expression.toString().equals("getClass()")) {
            /* implicit this case */
            typeRef = ((CtType) expression.getParent(CtType.class)).getReference();
        } else {
            typeRef = ((CtInvocation) expression).getTarget().getType();
        }

        Factory factory = element.getFactory();
        CtFieldAccess<?> classAccess = factory.Code().createClassAccess(typeRef);

        expression.replace(classAccess);
    }
}
