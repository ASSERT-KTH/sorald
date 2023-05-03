package sorald.processor;

import static sorald.util.Transformations.not;

import java.util.Collection;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

@ProcessorAnnotation(
        key = "S1155",
        description = "Collection.isEmpty() should be used to test for emptiness")
public class CollectionIsEmptyProcessor extends SoraldAbstractProcessor<CtBinaryOperator<?>> {

    @Override
    protected void repairInternal(CtBinaryOperator<?> element) {
        CtMethod<?> isEmptyMethod = getIsEmptyMethodReference(getFactory());

        if (element.getLeftHandOperand() instanceof CtInvocation) {
            CtExpression<?> methodCallTarget =
                    ((CtInvocation<?>) element.getLeftHandOperand()).getTarget();

            CtInvocation<?> newInvocation =
                    getFactory().createInvocation(methodCallTarget, isEmptyMethod.getReference());

            repairLeftHandOperand(element, newInvocation);
        } else if (element.getRightHandOperand() instanceof CtInvocation) {
            CtExpression<?> methodCallTarget =
                    ((CtInvocation<?>) element.getRightHandOperand()).getTarget();

            CtInvocation<?> newInvocation =
                    getFactory().createInvocation(methodCallTarget, isEmptyMethod.getReference());

            repairRightHandOperand(element, newInvocation);
        }
    }

    private static CtMethod<?> getIsEmptyMethodReference(Factory factory) {
        return factory.Type().get(Collection.class).getMethodsByName("isEmpty").stream()
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }

    private void repairLeftHandOperand(CtBinaryOperator<?> element, CtInvocation<?> newInvocation) {
        // == 0
        if (element.getKind() == BinaryOperatorKind.EQ) {
            element.replace(newInvocation);
        }
        // < 1
        else if (element.getKind() == BinaryOperatorKind.LT
                && element.getRightHandOperand().equals(getFactory().createLiteral(1))) {
            element.replace(newInvocation);
        }
        // <= 0
        else if (element.getKind() == BinaryOperatorKind.LE
                && element.getRightHandOperand().equals(getFactory().createLiteral(0))) {
            element.replace(newInvocation);
        }
        element.replace(not(newInvocation));
    }

    private void repairRightHandOperand(
            CtBinaryOperator<?> element, CtInvocation<?> newInvocation) {
        // 0 ==
        if (element.getKind() == BinaryOperatorKind.EQ) {
            element.replace(newInvocation);
        }
        // 1 >
        else if (element.getKind() == BinaryOperatorKind.GT
                && element.getLeftHandOperand().equals(getFactory().createLiteral(1))) {
            element.replace(newInvocation);
        }
        // 0 >=
        else if (element.getKind() == BinaryOperatorKind.GE
                && element.getLeftHandOperand().equals(getFactory().createLiteral(0))) {
            element.replace(newInvocation);
        }
        element.replace(not(newInvocation));
    }
}
