package sorald.processor;

import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtThrow;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.factory.Factory;

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

        CtStatement statementToInsert =
                mustTypeCheckCatchVariable(element)
                        ? wrapInTypeCheck(element.getParameter(), secondInvocation)
                        : secondInvocation;

        element.getBody()
                .addStatement(lastSafeInterruptIndex(element.getBody()), statementToInsert);
    }

    /**
     * Return the last index that is safe to insert at, safe meaning that the interrupt is certain
     * to actually be invoked.
     *
     * <p>This is either the first index at which a return statement or throw is found, or the index
     * past the last index if no returns or throws are found.
     */
    private static int lastSafeInterruptIndex(CtBlock<?> block) {
        for (int i = 0; i < block.getStatements().size(); i++) {
            if (containsReturnOrThrow(block.getStatement(i))) {
                return i;
            }
        }
        return block.getStatements().size();
    }

    /**
     * Check if the given statement contains a return or throw.
     *
     * <p>TODO recursively check method declarations if there are method calls.
     */
    private static boolean containsReturnOrThrow(CtStatement statement) {
        return !statement
                .filterChildren(e -> e instanceof CtReturn || e instanceof CtThrow)
                .list()
                .isEmpty();
    }

    private static boolean mustTypeCheckCatchVariable(CtCatch ctCatch) {
        return ctCatch.getParameter().getMultiTypes().size() != 1;
    }

    private static CtIf wrapInTypeCheck(CtVariable<?> varToCheck, CtStatement statementToWrap) {
        Factory fact = varToCheck.getFactory();
        CtVariableAccess<?> varRead = fact.createVariableRead(varToCheck.getReference(), false);
        CtTypeAccess<?> interruptedExcAccess =
                fact.createTypeAccess(fact.Type().get(InterruptedException.class).getReference());

        CtIf ctIf = fact.createIf();
        ctIf.setCondition(
                fact.createBinaryOperator(
                        varRead, interruptedExcAccess, BinaryOperatorKind.INSTANCEOF));

        // We add a block instead of just the naked statement here to force printing of curly
        // brackets
        CtBlock<?> thenBranch = fact.createCtBlock(statementToWrap);
        ctIf.setThenStatement(thenBranch);

        return ctIf;
    }
}
