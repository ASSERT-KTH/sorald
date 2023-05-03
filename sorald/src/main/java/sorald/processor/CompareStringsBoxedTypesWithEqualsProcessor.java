package sorald.processor;

import static sorald.util.Transformations.not;

import sorald.annotations.ProcessorAnnotation;

import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;

@ProcessorAnnotation(
        key = "S4973",
        description = "Strings and Boxed types should be compared using \"equals()\"")
public class CompareStringsBoxedTypesWithEqualsProcessor
        extends SoraldAbstractProcessor<CtBinaryOperator<?>> {

    @Override
    protected void repairInternal(CtBinaryOperator<?> element) {
        CtExpression<?> leftHandOperand = element.getLeftHandOperand();
        CtExpression<?> rightHandOperand = element.getRightHandOperand();

        // By default, we choose the left hand operand as the target of the invocation, so we check
        // if it is null
        CtExpression<?> lhs = getNullCheck(leftHandOperand);
        CtExpression<?> rhs =
                getComparisonWithEquals(leftHandOperand, rightHandOperand, element.getKind());

        CtBinaryOperator<?> transformedBinaryOperator =
                getFactory().createBinaryOperator(lhs, rhs, BinaryOperatorKind.AND);

        element.replace(transformedBinaryOperator);
    }

    private CtExpression<?> getNullCheck(CtExpression<?> operand) {
        CtExpression<?> nullLiteral = getFactory().createLiteral(null);
        return getFactory().createBinaryOperator(operand, nullLiteral, BinaryOperatorKind.NE);
    }

    private CtExpression<?> getComparisonWithEquals(
            CtExpression<?> leftOperand, CtExpression<?> rightOperand, BinaryOperatorKind kind) {
        CtMethod<?> equals =
                leftOperand.getType().getTypeDeclaration().getMethodsByName("equals").stream()
                        .findFirst()
                        .orElseThrow(IllegalStateException::new);
        CtInvocation<?> equalsInvocation =
                getFactory().createInvocation(leftOperand, equals.getReference(), rightOperand);
        if (kind == BinaryOperatorKind.EQ) {
            return equalsInvocation;
        } else if (kind == BinaryOperatorKind.NE) {
            return not(equalsInvocation);
        }
        throw new IllegalArgumentException("Unexpected binary operator kind: " + kind);
    }
}
