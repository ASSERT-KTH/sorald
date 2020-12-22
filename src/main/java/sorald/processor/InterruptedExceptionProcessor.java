package sorald.processor;

import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

@ProcessorAnnotation(key = 2142, description = "\"InterruptedException\" should not be ignored")
public class InterruptedExceptionProcessor extends SoraldAbstractProcessor<CtCatch> {

    @Override
    protected boolean canRepairInternal(CtCatch candidate) {
        return true;
    }

    @Override
    protected void repairInternal(CtCatch element) {
        Factory factory = element.getFactory();
        CtClass<?> threadClass = factory.Class().get(Thread.class);
        CtTypeAccess<?> threadClassAccess = factory.createTypeAccess(threadClass.getReference());
        CtMethod<?> currentThreadMethod = threadClass.getMethodsByName("currentThread").get(0);
        CtMethod<?> interruptMethod = threadClass.getMethodsByName("interrupt").get(0);
        CtInvocation firstInvocation =
                factory.createInvocation(threadClassAccess, currentThreadMethod.getReference());
        CtInvocation secondInvocation =
                factory.createInvocation(firstInvocation, interruptMethod.getReference());

        element.getBody().addStatement(element.getBody().getStatements().size(), secondInvocation);
    }
}
