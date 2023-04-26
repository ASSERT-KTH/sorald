package sorald.processor;

import static sorald.util.Transformations.not;

import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;

@ProcessorAnnotation(
        key = "S1155",
        description = "Collection.isEmpty() should be used to test for emptiness")
public class CollectionIsEmptyProcessor extends SoraldAbstractProcessor<CtBinaryOperator<?>> {

    @Override
    protected void repairInternal(CtBinaryOperator<?> element) {
        CtExpression<?> methodCallTarget =
                ((CtInvocation<?>) element.getLeftHandOperand()).getTarget();

        CtMethod<?> isEmptyMethod =
                methodCallTarget.getType().getTypeDeclaration().getMethodsByName("isEmpty").stream()
                        .findFirst()
                        .orElseThrow(IllegalStateException::new);
        CtInvocation<?> newInvocation =
                getFactory().createInvocation(methodCallTarget, isEmptyMethod.getReference());
        CtExpression<?> expression = getIsEmptyInvocation(newInvocation, element);
        element.replace(expression);
    }

    /**
     * Returns the invocation of isEmpty() that should be used to replace the given element. If the
     * operator is == or < 1, then we can just return the invocation, otherwise we need to negate
     * it.
     */
    private CtExpression<?> getIsEmptyInvocation(
            CtInvocation<?> invocation, CtBinaryOperator<?> element) {
        if (element.getKind() == BinaryOperatorKind.EQ) {
            return invocation;
        } else if (element.getKind() == BinaryOperatorKind.LT
                && element.getRightHandOperand().equals(getFactory().createLiteral(1))) {
            return invocation;
        }
        return not(invocation);
    }
}
