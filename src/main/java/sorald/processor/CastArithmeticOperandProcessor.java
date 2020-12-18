package sorald.processor;

import java.util.List;
import sorald.Constants;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

@ProcessorAnnotation(key = 2184, description = "Math operands should be cast before assignment")
public class CastArithmeticOperandProcessor extends SoraldAbstractProcessor<CtBinaryOperator> {

    @Override
    protected boolean canRepairInternal(CtBinaryOperator candidate) {
        List<CtBinaryOperator> binaryOperatorChildren =
                candidate.getElements(new TypeFilter<>(CtBinaryOperator.class));
        if (binaryOperatorChildren.size()
                == 1) { // in a nested binary operator expression, only one will be processed.
            if (isArithmeticOperation(candidate) && isExpIntAndOrLong(candidate)) {
                CtTypeReference ctType = getExpectedType(candidate);
                if (ctType != null) {
                    if (isTypeLongOrDoubleOrFloat(ctType)
                            && !(isLongPartOfTheExp(candidate) && isTypeLong(ctType))
                            && !(candidate.getKind().compareTo(BinaryOperatorKind.DIV) == 0
                                    && isExpFullyInt(candidate)
                                    && isTypeLong(ctType))
                            && !checkDivisionInParents(candidate)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void repairInternal(CtBinaryOperator element) {
        CtTypeReference<?> typeToBeUsedToCast = getExpectedType(element);
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

    private CtTypeReference getExpectedType(CtBinaryOperator ctBinaryOperator) {
        CtTypeReference ctTypeReference = null;

        if (ctBinaryOperator.getParent(CtAbstractInvocation.class) != null) {

            CtAbstractInvocation ctAbstractInvocation =
                    ctBinaryOperator.getParent(CtAbstractInvocation.class);
            List<CtExpression> arguments = ctAbstractInvocation.getArguments();

            int indexInInvocation = -1;
            for (int i = 0; i < arguments.size(); i++) {
                if (arguments.get(i) == ctBinaryOperator) {
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

    private boolean isArithmeticOperation(CtBinaryOperator ctBinaryOperator) {
        return ctBinaryOperator.getKind().compareTo(BinaryOperatorKind.PLUS) == 0
                || ctBinaryOperator.getKind().compareTo(BinaryOperatorKind.MINUS) == 0
                || ctBinaryOperator.getKind().compareTo(BinaryOperatorKind.MUL) == 0
                || ctBinaryOperator.getKind().compareTo(BinaryOperatorKind.DIV) == 0;
    }

    private boolean isExpIntAndOrLong(CtBinaryOperator ctBinaryOperator) {
        return (ctBinaryOperator
                                .getLeftHandOperand()
                                .getType()
                                .getSimpleName()
                                .equals(Constants.INT)
                        || ctBinaryOperator
                                .getLeftHandOperand()
                                .getType()
                                .getSimpleName()
                                .equals(Constants.LONG))
                && (ctBinaryOperator
                                .getRightHandOperand()
                                .getType()
                                .getSimpleName()
                                .equals(Constants.INT)
                        || ctBinaryOperator
                                .getRightHandOperand()
                                .getType()
                                .getSimpleName()
                                .equals(Constants.LONG));
    }

    private boolean isFloatingPoint(CtBinaryOperator ctBinaryOperator) {
        return ctBinaryOperator
                        .getLeftHandOperand()
                        .getType()
                        .getSimpleName()
                        .equals(Constants.DOUBLE)
                || ctBinaryOperator
                        .getLeftHandOperand()
                        .getType()
                        .getSimpleName()
                        .equals(Constants.FLOAT)
                || ctBinaryOperator
                        .getRightHandOperand()
                        .getType()
                        .getSimpleName()
                        .equals(Constants.DOUBLE)
                || ctBinaryOperator
                        .getRightHandOperand()
                        .getType()
                        .getSimpleName()
                        .equals(Constants.FLOAT);
    }

    private boolean isExpFullyInt(CtBinaryOperator ctBinaryOperator) {
        return ctBinaryOperator.getLeftHandOperand().getType().getSimpleName().equals(Constants.INT)
                && ctBinaryOperator
                        .getRightHandOperand()
                        .getType()
                        .getSimpleName()
                        .equals(Constants.INT);
    }

    private boolean isLongPartOfTheExp(CtBinaryOperator ctBinaryOperator) {
        return ctBinaryOperator
                        .getLeftHandOperand()
                        .getType()
                        .getSimpleName()
                        .equals(Constants.LONG)
                || ctBinaryOperator
                        .getRightHandOperand()
                        .getType()
                        .getSimpleName()
                        .equals(Constants.LONG);
    }

    private boolean isTypeLongOrDoubleOrFloat(CtTypeReference ctTypeReference) {
        return ctTypeReference.getSimpleName().equals(Constants.LONG)
                || ctTypeReference.getSimpleName().equals(Constants.DOUBLE)
                || ctTypeReference.getSimpleName().equals(Constants.FLOAT);
    }

    private boolean isTypeLong(CtTypeReference ctTypeReference) {
        return ctTypeReference.getSimpleName().equals(Constants.LONG);
    }

    private boolean checkDivisionInParents(CtBinaryOperator ctBinaryOperator) {
        CtElement parent = ctBinaryOperator;
        while (parent != null && parent instanceof CtBinaryOperator) {
            if (((CtBinaryOperator) parent).getKind().compareTo(BinaryOperatorKind.DIV) == 0) {
                if (isFloatingPoint((CtBinaryOperator) parent)) {
                    return true;
                }
                break;
            }
            parent = parent.getParent();
        }
        return false;
    }
}
