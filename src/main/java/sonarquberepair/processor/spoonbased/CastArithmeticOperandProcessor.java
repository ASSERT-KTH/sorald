package sonarquberepair.processor.spoonbased;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;

import sonarquberepair.UniqueTypesCollector;

public class CastArithmeticOperandProcessor extends AbstractProcessor<CtBinaryOperator> {

    private CtTypeReference typeToBeUsedToCast;

    @Override
    public boolean isToBeProcessed(CtBinaryOperator candidate) {
        List<CtBinaryOperator> binaryOperatorChildren = candidate.getElements(new TypeFilter<>(CtBinaryOperator.class));
        if (binaryOperatorChildren.size() == 1) { // in a nested binary operator expression, only one will be processed.
            if (isArithmeticOperation(candidate) && isExpIntAndOrLong(candidate)) {
                CtTypeReference ctType = getExpectedType(candidate);
                if (ctType != null) {
                    if (isTypeLongOrDoubleOrFloat(ctType) &&
                            !(isLongPartOfTheExp(candidate) && isTypeLong(ctType)) &&
                            !(candidate.getKind().compareTo(BinaryOperatorKind.DIV) == 0 && isExpFullyInt(candidate) && isTypeLong(ctType)) &&
                            !checkDivisionInParents(candidate)) {
                            typeToBeUsedToCast = ctType;
                            return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void process(CtBinaryOperator element) {
        UniqueTypesCollector.getInstance().collect(element);

        CtCodeSnippetExpression newBinaryOperator = element.getFactory().createCodeSnippetExpression("(" + typeToBeUsedToCast.getSimpleName() + ") " + element.getLeftHandOperand());
        element.setLeftHandOperand(newBinaryOperator);

        // A nicer code for the casting would be the next line. However, more parentheses are added in the expressions when using such a solution.
        // element.getLeftHandOperand().addTypeCast(typeToBeUsedToCast.clone());
    }

    private CtTypeReference getExpectedType(CtBinaryOperator ctBinaryOperator) {
        CtTypeReference ctTypeReference = null;

        if (ctBinaryOperator.getParent(CtAbstractInvocation.class) != null) {

            CtAbstractInvocation ctAbstractInvocation = ctBinaryOperator.getParent(CtAbstractInvocation.class);
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
                ctTypeReference = (CtTypeReference) ctExecutableReference.getParameters().get(indexInInvocation);
            }

        } else if (ctBinaryOperator.getParent(CtField.class) != null ||
                ctBinaryOperator.getParent(CtLocalVariable.class) != null) {
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
        return ctBinaryOperator.getKind().compareTo(BinaryOperatorKind.PLUS) == 0 ||
                ctBinaryOperator.getKind().compareTo(BinaryOperatorKind.MINUS) == 0 ||
                ctBinaryOperator.getKind().compareTo(BinaryOperatorKind.MUL) == 0 ||
                ctBinaryOperator.getKind().compareTo(BinaryOperatorKind.DIV) == 0;
    }

    private boolean isExpIntAndOrLong(CtBinaryOperator ctBinaryOperator) {
        return (ctBinaryOperator.getLeftHandOperand().getType().getSimpleName().equals("int") ||
                ctBinaryOperator.getLeftHandOperand().getType().getSimpleName().equals("long")) &&
                (ctBinaryOperator.getRightHandOperand().getType().getSimpleName().equals("int") ||
                ctBinaryOperator.getRightHandOperand().getType().getSimpleName().equals("long"));
    }

    private boolean isFloatingPoint(CtBinaryOperator ctBinaryOperator) {
        return ctBinaryOperator.getLeftHandOperand().getType().getSimpleName().equals("double") ||
                ctBinaryOperator.getLeftHandOperand().getType().getSimpleName().equals("float") ||
                ctBinaryOperator.getRightHandOperand().getType().getSimpleName().equals("double") ||
                ctBinaryOperator.getRightHandOperand().getType().getSimpleName().equals("float");
    }

    private boolean isExpFullyInt(CtBinaryOperator ctBinaryOperator) {
        return ctBinaryOperator.getLeftHandOperand().getType().getSimpleName().equals("int") &&
                ctBinaryOperator.getRightHandOperand().getType().getSimpleName().equals("int");
    }

    private boolean isLongPartOfTheExp(CtBinaryOperator ctBinaryOperator) {
        return ctBinaryOperator.getLeftHandOperand().getType().getSimpleName().equals("long") ||
                ctBinaryOperator.getRightHandOperand().getType().getSimpleName().equals("long");
    }

    private boolean isTypeLongOrDoubleOrFloat(CtTypeReference ctTypeReference) {
        return ctTypeReference.getSimpleName().equals("long") ||
                ctTypeReference.getSimpleName().equals("double") ||
                ctTypeReference.getSimpleName().equals("float");
    }

    private boolean isTypeLong(CtTypeReference ctTypeReference) {
        return ctTypeReference.getSimpleName().equals("long");
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
