package sonarquberepair.processor;

import org.sonar.java.checks.MathOnFloatCheck;
import sonarquberepair.ProcessorAnnotation;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;

@ProcessorAnnotation(description="Math should not be performed on floats")
public class MathOnFloatProcessor extends SQRAbstractProcessor<CtBinaryOperator> {

    public MathOnFloatProcessor(String originalFilesPath) {
        super(originalFilesPath, new MathOnFloatCheck());
    }

    @Override
    public boolean isToBeProcessed(CtBinaryOperator candidate) {
        if (!super.isToBeProcessedAccordingToSonar(candidate)) {
            return false;
        }
        List<CtBinaryOperator> binaryOperatorChildren = candidate.getElements(new TypeFilter<>(CtBinaryOperator.class));
        if (binaryOperatorChildren.size() == 1) { // in a nested binary operator expression, only one will be processed.
            if (isArithmeticOperation(candidate) && isOperationBetweenFloats(candidate) && !withinStringConcatenation(candidate)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void process(CtBinaryOperator element) {
        super.process(element);

        CtCodeSnippetExpression newLeftHandOperand = element.getFactory().createCodeSnippetExpression("(double) " + element.getLeftHandOperand());
        element.setLeftHandOperand(newLeftHandOperand);
        CtCodeSnippetExpression newRightHandOperand = element.getFactory().createCodeSnippetExpression("(double) " + element.getRightHandOperand());
        element.setRightHandOperand(newRightHandOperand);
    }

    private boolean isArithmeticOperation(CtBinaryOperator ctBinaryOperator) {
        return ctBinaryOperator.getKind().compareTo(BinaryOperatorKind.PLUS) == 0 ||
                ctBinaryOperator.getKind().compareTo(BinaryOperatorKind.MINUS) == 0 ||
                ctBinaryOperator.getKind().compareTo(BinaryOperatorKind.MUL) == 0 ||
                ctBinaryOperator.getKind().compareTo(BinaryOperatorKind.DIV) == 0;
    }

    private boolean isOperationBetweenFloats(CtBinaryOperator ctBinaryOperator) {
        return ctBinaryOperator.getLeftHandOperand().getType().getSimpleName().equals("float") &&
                ctBinaryOperator.getRightHandOperand().getType().getSimpleName().equals("float");
    }

    private boolean withinStringConcatenation(CtBinaryOperator ctBinaryOperator) {
        CtElement parent = ctBinaryOperator;
        while (parent.getParent() instanceof CtBinaryOperator) {
            parent = parent.getParent();
        }
        return ((CtBinaryOperator) parent).getKind().compareTo(BinaryOperatorKind.PLUS) == 0 &&
                (((CtBinaryOperator) parent).getLeftHandOperand().getType().getQualifiedName().equals("java.lang.String") ||
                        ((CtBinaryOperator) parent).getRightHandOperand().getType().getQualifiedName().equals("java.lang.String"));
    }

}
