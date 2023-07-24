package sorald.processor;

import java.util.Set;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtCatchVariable;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLabelledFlowBreak;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtThrow;
import spoon.reflect.code.CtTry;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

@ProcessorAnnotation(key = "S2142", description = "\"InterruptedException\" should not be ignored")
public class InterruptedExceptionProcessor extends SoraldAbstractProcessor<CtCatch> {

    @Override
    protected void repairInternal(CtCatch element) {
        Factory factory = element.getFactory();
        CtClass<?> threadClass = factory.Class().get(Thread.class);
        CtTypeAccess<?> threadClassAccess = factory.createTypeAccess(threadClass.getReference());
        CtMethod<?> currentThreadMethod = threadClass.getMethodsByName("currentThread").get(0);
        CtMethod<?> interruptMethod = threadClass.getMethodsByName("interrupt").get(0);
        CtInvocation<?> firstInvocation =
                factory.createInvocation(threadClassAccess, currentThreadMethod.getReference());
        CtInvocation<?> secondInvocation =
                factory.createInvocation(firstInvocation, interruptMethod.getReference());

        if (mustTypeCheckCatchVariable(element)) {
            wrapInCatcher(element, secondInvocation);
        } else {
            element.getBody()
                    .addStatement(lastSafeInterruptIndex(element.getBody()), secondInvocation);
        }
    }

    private static void wrapInCatcher(CtCatch violatedCatch, CtStatement statementToWrap) {
        Factory factory = violatedCatch.getFactory();
        CtTypeReference<?> refToInterruptedException =
                factory.Type().get(InterruptedException.class).getReference();

        // Remove InterruptedException from the catch
        CtCatchVariable<?> catchVariable = violatedCatch.getParameter();
        catchVariable.removeMultiType(refToInterruptedException);

        // Add statement into a cloned catch block
        // Currently, we clone the body of the violated catch which is theoretically wrong because
        // there could be statements concerning only one of the exception types.
        // Example - `if (e instanceof IOException) { ... }`. However, it is safe // NOSONAR:S125
        // to ignore this case because it is rare.
        CtBlock<?> blockForNewCatcher = violatedCatch.getBody().clone();
        blockForNewCatcher.addStatement(
                lastSafeInterruptIndex(blockForNewCatcher), statementToWrap);

        // Set the variable and body for the new catcher
        CtCatch newCatch = factory.createCatch();
        newCatch.setBody(blockForNewCatcher);
        CtCatchVariable<?> newCatchVariable =
                factory.createCatchVariable(
                        refToInterruptedException, catchVariable.getSimpleName());
        newCatch.setParameter((CtCatchVariable<? extends Throwable>) newCatchVariable);

        CtTry tryOfViolatedCatcher = violatedCatch.getParent(CtTry.class);
        int indexOfViolatedCatch = tryOfViolatedCatcher.getCatchers().indexOf(violatedCatch);
        int indexOfNewCatch = getNewCatchIndex(violatedCatch, indexOfViolatedCatch);
        tryOfViolatedCatcher.addCatcherAt(indexOfNewCatch, newCatch);
    }

    /**
     * Return the last index that is safe to insert at, safe meaning that the interrupt is certain
     * to actually be invoked.
     *
     * <p>This is either the first index at which some flow breaks are found, or the index past the
     * last index if none found.
     */
    private static int lastSafeInterruptIndex(CtBlock<?> block) {
        for (int i = 0; i < block.getStatements().size(); i++) {
            if (containsReturnOrThrowOrLabelledFlowBreak(block.getStatement(i))) {
                return i;
            }
        }
        return block.getStatements().size();
    }

    private static int getNewCatchIndex(CtCatch violatedCatch, int indexOfViolatedCatch) {
        CtTypeReference<?> refToInterruptedException =
                violatedCatch.getFactory().Type().get(InterruptedException.class).getReference();
        Set<CtTypeReference<?>> catchTypes = violatedCatch.getParameter().getReferencedTypes();
        for (CtTypeReference<?> catchType : catchTypes) {
            if (refToInterruptedException.isSubtypeOf(catchType)) {
                return indexOfViolatedCatch;
            }
        }
        return indexOfViolatedCatch + 1;
    }

    /**
     * Check if the given statement contains a return, throw, or a labelled flow break.
     *
     * <p>TODO recursively check method declarations if there are method calls.
     */
    private static boolean containsReturnOrThrowOrLabelledFlowBreak(CtStatement statement) {
        return !statement
                .filterChildren(
                        e ->
                                e instanceof CtReturn
                                        || e instanceof CtThrow
                                        || e instanceof CtLabelledFlowBreak)
                .list()
                .isEmpty();
    }

    private static boolean mustTypeCheckCatchVariable(CtCatch ctCatch) {
        return ctCatch.getParameter().getMultiTypes().size() != 1;
    }
}
