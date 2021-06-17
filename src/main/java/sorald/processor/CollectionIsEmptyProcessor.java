package sorald.processor;

import static sorald.util.Transformations.not;

import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;

@ProcessorAnnotation(
        key = 1155,
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
        CtExpression<?> expression =
                element.getKind() == BinaryOperatorKind.EQ ? newInvocation : not(newInvocation);
        element.replace(expression);
    }
}
