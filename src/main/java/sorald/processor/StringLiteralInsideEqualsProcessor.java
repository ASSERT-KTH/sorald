package sorald.processor;

import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtExecutableReference;

@ProcessorAnnotation(
        key = 1132,
        description =
                "Strings literals should be placed on the left side when checking for equality")
public class StringLiteralInsideEqualsProcessor extends SoraldAbstractProcessor<CtInvocation<?>> {

    @Override
    protected void repairInternal(CtInvocation<?> element) {
        if (element.getExecutable().getSignature().equals("equals(java.lang.Object)")) {
            CtType stringClass = getFactory().Class().get(String.class);
            CtMethod ctMethodToBeCalled = (CtMethod) stringClass.getMethodsByName("equals").get(0);
            CtExecutableReference ctExecutableReferenceToMethodToBeCalled =
                    getFactory().Executable().createReference(ctMethodToBeCalled);

            CtInvocation newInvocation =
                    getFactory()
                            .Code()
                            .createInvocation(
                                    element.getArguments().get(0),
                                    ctExecutableReferenceToMethodToBeCalled,
                                    element.getTarget());

            element.replace(newInvocation);

            // the following is to handle the case in which there is a null check on the variable used as target
            CtExpression<?> variable = (CtExpression<?>) newInvocation.getArguments().get(0);
            Boolean nullCheck = false;
            CtElement parent = element.getParent();
            CtBinaryOperator parentBinaryOperator = null;
            while (parent instanceof CtBinaryOperator && !nullCheck) {
                parentBinaryOperator = (CtBinaryOperator) parent;
                CtElement parentLeftHandOperand = parentBinaryOperator.getLeftHandOperand();
                if (parentLeftHandOperand instanceof CtBinaryOperator &&
                        isNullCheckOnTheVariable((CtBinaryOperator) parentLeftHandOperand, variable)) {
                    nullCheck = true;
                } else {
                    parent = element.getParent();
                }
            }
            if (nullCheck) {
                parentBinaryOperator.replace(newInvocation);
            }
        }
    }

    private boolean isNullCheckOnTheVariable(CtBinaryOperator ctBinaryOperator, CtExpression<?> variable) {
        return ctBinaryOperator.getKind().equals(BinaryOperatorKind.NE) &&
                ((ctBinaryOperator.getLeftHandOperand().equals(variable) &&
                        ctBinaryOperator.getRightHandOperand().toString().equals("null")) ||
                        (ctBinaryOperator.getLeftHandOperand().toString().equals("null") &&
                        ctBinaryOperator.getRightHandOperand().equals(variable)));
    }
}
