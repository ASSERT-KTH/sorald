package sorald.processor;

import java.util.Arrays;
import sorald.Constants;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;

@ProcessorAnnotation(
        key = 2116,
        description = "\"hashCode\" and \"toString\" should not be called on array instances")
public class ArrayHashCodeAndToStringProcessor extends SoraldAbstractProcessor<CtInvocation<?>> {

    @Override
    protected boolean canRepairInternal(CtInvocation<?> candidate) {
        CtExpression<?> target = candidate.getTarget();
        if (target == null || target.getType() == null) {
            return false;
        }
        if (target.getType().isArray()) {
            if (candidate
                            .getExecutable()
                            .getSignature()
                            .equals(Constants.TOSTRING_METHOD_NAME + "()")
                    || (candidate
                            .getExecutable()
                            .getSignature()
                            .equals(Constants.HASHCODE_METHOD_NAME + "()"))) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void repairInternal(CtInvocation<?> element) {
        CtExpression prevTarget = element.getTarget();
        CtClass arraysClass = getFactory().Class().get(Arrays.class);
        CtTypeAccess<?> newTarget = getFactory().createTypeAccess(arraysClass.getReference());
        CtMethod method = null;
        if (element.getExecutable().getSignature().equals(Constants.HASHCODE_METHOD_NAME + "()")) {
            method = (CtMethod) arraysClass.getMethodsByName(Constants.HASHCODE_METHOD_NAME).get(0);
        } else if (element.getExecutable()
                .getSignature()
                .equals(Constants.TOSTRING_METHOD_NAME + "()")) {
            method = (CtMethod) arraysClass.getMethodsByName(Constants.TOSTRING_METHOD_NAME).get(0);
        } else {
            System.err.println("Unhandled case. Something went wrong.");
        }
        CtExecutableReference refToMethod = getFactory().Executable().createReference(method);
        CtInvocation newInvocation =
                getFactory().Code().createInvocation(newTarget, refToMethod, prevTarget);
        element.replace(newInvocation);
    }
}
