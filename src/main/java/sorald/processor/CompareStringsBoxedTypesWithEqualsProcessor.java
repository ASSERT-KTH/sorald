package sorald.processor;

import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.UnaryOperatorKind;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtTypeReference;

@ProcessorAnnotation(
        key = 4973,
        description = "Strings and Boxed types should be compared using \"equals()\"")
public class CompareStringsBoxedTypesWithEqualsProcessor
        extends SoraldAbstractProcessor<CtBinaryOperator<?>> {

    @Override
    protected boolean canRepairInternal(CtBinaryOperator<?> candidate) {
        BinaryOperatorKind opKind = candidate.getKind();
        if (opKind == BinaryOperatorKind.EQ || opKind == BinaryOperatorKind.NE) {
            CtExpression<?> left = candidate.getLeftHandOperand();
            CtExpression<?> right = candidate.getRightHandOperand();
            CtTypeReference<?> lType = left.getType();
            CtTypeReference<?> rType = right.getType();
            /*
            The reason we don't check for the case where one variable is boxed is because Java implicitly unboxes
            the boxed type to primitive, making the == check fine. See JLS #5.6.2:
            https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.6.2
            */
            CtTypeReference<?> stringType = getFactory().Type().STRING;

            /*
            Case 1: Both variables are strings.
            Case 2: The left variable is a string and the right one is boxed.
            Case 3: The left variable is boxed and the right one is a string.
            Case 4: Both variables are boxed.
            */
            return (lType != null && rType != null)
                    && ((lType.equals(stringType) && rType.equals(stringType))
                            || (lType.equals(stringType) && !rType.unbox().equals(rType))
                            || (!lType.unbox().equals(lType) && rType.equals(stringType))
                            || (!lType.unbox().equals(lType) && !rType.unbox().equals(rType)));
        } else {
            return false;
        }
    }

    @Override
    protected void repairInternal(CtBinaryOperator<?> element) {
        CtExpression<?> lhs = element.getLeftHandOperand();
        CtExpression<?> rhs = element.getRightHandOperand();

        CtMethod<?> equals =
                lhs.getType().getTypeDeclaration().getMethodsByName("equals").stream()
                        .findFirst()
                        .orElseThrow(IllegalStateException::new);
        CtInvocation<?> lhsEqualsRhs =
                getFactory().createInvocation(lhs, equals.getReference(), rhs);
        CtExpression<?> expr =
                element.getKind() == BinaryOperatorKind.NE ? not(lhsEqualsRhs) : lhsEqualsRhs;
        element.replace(expr);
    }

    private <T> CtUnaryOperator<T> not(CtExpression<T> expr) {
        CtUnaryOperator<T> op = getFactory().createUnaryOperator();
        op.setKind(UnaryOperatorKind.NOT);
        op.setOperand(expr);
        return op;
    }
}
