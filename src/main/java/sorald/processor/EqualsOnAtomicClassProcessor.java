package sorald.processor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtExecutableReference;

@ProcessorAnnotation(
        key = 2204,
        description = "\".equals()\" should not be used to test the values of \"Atomic\" classes")
public class EqualsOnAtomicClassProcessor extends SoraldAbstractProcessor<CtInvocation> {

    @Override
    protected boolean canRepairInternal(CtInvocation candidate) {
        if (candidate.getExecutable().getSignature().equals("equals(java.lang.Object)")
                && isAtomicClassRef(candidate.getTarget())) {
            return true;
        }
        return false;
    }

    @Override
    protected void repairInternal(CtInvocation element) {
        CtType atomicClass;
        if (isAtomicInteger(element.getTarget())) {
            atomicClass = getFactory().Class().get(AtomicInteger.class);
        } else if (isAtomicLong(element.getTarget())) {
            atomicClass = getFactory().Class().get(AtomicLong.class);
        } else {
            atomicClass = getFactory().Class().get(AtomicBoolean.class);
        }

        CtMethod ctMethodToBeCalled = (CtMethod) atomicClass.getMethodsByName("get").get(0);
        CtExecutableReference ctExecutableReferenceToMethodToBeCalled =
                getFactory().Executable().createReference(ctMethodToBeCalled);

        CtInvocation leftInvocation =
                getFactory()
                        .Code()
                        .createInvocation(
                                element.getTarget(), ctExecutableReferenceToMethodToBeCalled);
        CtInvocation rightInvocation =
                getFactory()
                        .Code()
                        .createInvocation(
                                (CtExpression) element.getArguments().get(0),
                                ctExecutableReferenceToMethodToBeCalled);

        CtBinaryOperator newCtBinaryOperator =
                getFactory()
                        .Code()
                        .createBinaryOperator(
                                leftInvocation, rightInvocation, BinaryOperatorKind.EQ);

        element.replace(newCtBinaryOperator);
    }

    private boolean isAtomicClassRef(CtExpression ctExpression) {
        return isAtomicInteger(ctExpression)
                || isAtomicLong(ctExpression)
                || isAtomicBoolean(ctExpression);
    }

    private boolean isAtomicInteger(CtExpression ctExpression) {
        return ctExpression
                .getType()
                .getQualifiedName()
                .equals("java.util.concurrent.atomic.AtomicInteger");
    }

    private boolean isAtomicLong(CtExpression ctExpression) {
        return ctExpression
                .getType()
                .getQualifiedName()
                .equals("java.util.concurrent.atomic.AtomicLong");
    }

    private boolean isAtomicBoolean(CtExpression ctExpression) {
        return ctExpression
                .getType()
                .getQualifiedName()
                .equals("java.util.concurrent.atomic.AtomicBoolean");
    }
}
