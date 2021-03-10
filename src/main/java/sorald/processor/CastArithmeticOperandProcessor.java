package sorald.processor;

import java.util.List;
import java.util.Map;
import sorald.Constants;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

@ProcessorAnnotation(key = 2184, description = "Math operands should be cast before assignment")
public class CastArithmeticOperandProcessor extends SoraldAbstractProcessor<CtBinaryOperator> {

    @Override
    protected void repairInternal(CtBinaryOperator element) {
        CtTypeReference<?> typeToBeUsedToCast = getExpectedType(element);
        CtExpression<?> lhs = element.getLeftHandOperand();
        CtExpression<?> rhs = element.getRightHandOperand();

        if (isIntLiteral(lhs)) {
            repairWithLiteralSuffix((CtLiteral<?>) lhs, typeToBeUsedToCast);
        } else if (isIntLiteral(rhs)) {
            repairWithLiteralSuffix((CtLiteral<?>) rhs, typeToBeUsedToCast);
        } else {
            repairWithCast(element, typeToBeUsedToCast);
        }
    }

    private CtTypeReference getExpectedType(CtBinaryOperator ctBinaryOperator) {
        CtTypeReference ctTypeReference = null;

        if (ctBinaryOperator.getParent(CtAbstractInvocation.class) != null) {

            CtAbstractInvocation ctAbstractInvocation =
                    ctBinaryOperator.getParent(CtAbstractInvocation.class);
            List<CtExpression> arguments = ctAbstractInvocation.getArguments();

            CtElement argToBeFound = ctBinaryOperator;
            while (argToBeFound.getParent() != ctAbstractInvocation) {
                argToBeFound = argToBeFound.getParent();
            }

            int indexInInvocation = -1;
            for (int i = 0; i < arguments.size(); i++) {
                if (arguments.get(i) == argToBeFound) {
                    indexInInvocation = i;
                    break;
                }
            }

            CtExecutableReference ctExecutableReference = ctAbstractInvocation.getExecutable();
            if (ctExecutableReference != null && ctExecutableReference.getParameters() != null) {
                ctTypeReference =
                        (CtTypeReference)
                                ctExecutableReference.getParameters().get(indexInInvocation);
            }

        } else if (ctBinaryOperator.getParent(CtField.class) != null
                || ctBinaryOperator.getParent(CtLocalVariable.class) != null) {
            CtField ctField = ctBinaryOperator.getParent(CtField.class);
            CtLocalVariable ctLocalVariable = ctBinaryOperator.getParent(CtLocalVariable.class);
            ctTypeReference = ctField != null ? ctField.getType() : ctLocalVariable.getType();
        } else if (ctBinaryOperator.getParent(CtAssignment.class) != null) {
            CtAssignment ctAssignment = ctBinaryOperator.getParent(CtAssignment.class);
            if (!(ctAssignment instanceof CtOperatorAssignment)) {
                ctTypeReference = ctAssignment.getType();
            }
        } else if (ctBinaryOperator.getParent(CtReturn.class) != null) {
            CtReturn ctReturn = ctBinaryOperator.getParent(CtReturn.class);
            ctTypeReference = ctReturn.getParent(CtMethod.class).getType();
        }

        return ctTypeReference;
    }

    private static void repairWithCast(
            CtBinaryOperator element, CtTypeReference<?> typeToBeUsedToCast) {
        CtCodeSnippetExpression newBinaryOperator =
                element.getFactory()
                        .createCodeSnippetExpression(
                                "("
                                        + typeToBeUsedToCast.getSimpleName()
                                        + ") "
                                        + element.getLeftHandOperand());
        element.setLeftHandOperand(newBinaryOperator);
        // A nicer code for the casting would be the next line. However, more parentheses are added
        // in
        // the expressions when using such a solution.
        // element.getLeftHandOperand().addTypeCast(typeToBeUsedToCast.clone());
    }

    private static void repairWithLiteralSuffix(
            CtLiteral<?> literalInt, CtTypeReference<?> typeForSuffix) {
        Integer value = (Integer) literalInt.getValue();
        CtCodeSnippetExpression<?> literalWithSuffix =
                literalInt
                        .getFactory()
                        .createCodeSnippetExpression(
                                value + getLiteralSuffix(typeForSuffix).toUpperCase());
        literalInt.replace(literalWithSuffix);
    }

    private static boolean isIntLiteral(CtExpression<?> expr) {
        return expr instanceof CtLiteral
                && expr.getFactory().Type().INTEGER_PRIMITIVE.equals(expr.getType());
    }

    private static String getLiteralSuffix(CtTypeReference<?> floatDoubleOrLong) {
        String simpleName = floatDoubleOrLong.getSimpleName().toLowerCase();
        assert List.of(Constants.FLOAT, Constants.DOUBLE, Constants.LONG).contains(simpleName);
        return Map.of(Constants.FLOAT, "f", Constants.DOUBLE, "d", Constants.LONG, "l")
                .get(simpleName);
    }
}
