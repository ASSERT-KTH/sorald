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
}
