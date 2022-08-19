package sorald.processor;

import java.util.List;
import sorald.Constants;
import sorald.annotations.IncompleteProcessor;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.visitor.filter.TypeFilter;

@IncompleteProcessor(
        description =
                "does not cast the operands to double when the expected type of the result is float.")
@ProcessorAnnotation(key = "S2164", description = "Math should not be performed on floats")
public class MathOnFloatProcessor extends SoraldAbstractProcessor<CtBinaryOperator> {

    @Override
    protected boolean canRepairInternal(CtBinaryOperator candidate) {
        CtElement parentOfCandidate = candidate.getParent();

        if (parentOfCandidate instanceof CtTypedElement<?>) {
            if (((CtTypedElement<?>) parentOfCandidate)
                    .getType()
                    .equals(getFactory().Type().floatPrimitiveType())) {
                return false;
            }
        }

        return isArithmeticOperation(candidate)
                && isOperationBetweenFloats(candidate)
                && !withinStringConcatenation(candidate);
    }

    @Override
    protected void repairInternal(CtBinaryOperator element) {
        List<CtBinaryOperator> binaryOperatorChildren =
                element.getElements(new TypeFilter<>(CtBinaryOperator.class));
        for (CtBinaryOperator binaryOperator : binaryOperatorChildren) {
            if (binaryOperator.getLeftHandOperand() instanceof CtBinaryOperator<?>) {
                repairInternal((CtBinaryOperator<?>) binaryOperator.getLeftHandOperand());
            }
            if (binaryOperator.getRightHandOperand() instanceof CtBinaryOperator<?>) {
                repairInternal((CtBinaryOperator<?>) binaryOperator.getRightHandOperand());
            } else {
                if (!binaryOperator.getType().equals(getFactory().Type().doublePrimitiveType())) {
                    binaryOperator
                            .getLeftHandOperand()
                            .setTypeCasts(List.of(getFactory().Type().doublePrimitiveType()));

                    /**
                     * We also set the type so that the other operand is not explicitly cast as JVM
                     * implicitly does that For example, `(double) a + (double) b` is equivalent to
                     * `(double) a + b`. Thus, we provide a cleaner repair.
                     */
                    binaryOperator.setType(getFactory().Type().doublePrimitiveType());
                }
                // We do not need to cast the type of the right hand operand as it is already a
                // double
            }
        }
    }

    private boolean isArithmeticOperation(CtBinaryOperator ctBinaryOperator) {
        return ctBinaryOperator.getKind().compareTo(BinaryOperatorKind.PLUS) == 0
                || ctBinaryOperator.getKind().compareTo(BinaryOperatorKind.MINUS) == 0
                || ctBinaryOperator.getKind().compareTo(BinaryOperatorKind.MUL) == 0
                || ctBinaryOperator.getKind().compareTo(BinaryOperatorKind.DIV) == 0;
    }

    private boolean isOperationBetweenFloats(CtBinaryOperator ctBinaryOperator) {
        return ctBinaryOperator
                        .getLeftHandOperand()
                        .getType()
                        .getSimpleName()
                        .equals(Constants.FLOAT)
                && ctBinaryOperator
                        .getRightHandOperand()
                        .getType()
                        .getSimpleName()
                        .equals(Constants.FLOAT);
    }

    private boolean withinStringConcatenation(CtBinaryOperator ctBinaryOperator) {
        CtElement parent = ctBinaryOperator;
        while (parent.getParent() instanceof CtBinaryOperator) {
            parent = parent.getParent();
        }
        return ((CtBinaryOperator) parent).getKind().compareTo(BinaryOperatorKind.PLUS) == 0
                && (((CtBinaryOperator) parent)
                                .getLeftHandOperand()
                                .getType()
                                .getQualifiedName()
                                .equals(Constants.STRING_QUALIFIED_NAME)
                        || ((CtBinaryOperator) parent)
                                .getRightHandOperand()
                                .getType()
                                .getQualifiedName()
                                .equals(Constants.STRING_QUALIFIED_NAME));
    }
}
