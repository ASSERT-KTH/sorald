package sorald.processor;

import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

@ProcessorAnnotation(key = 1217, description = "\"Thread.run()\" should not be called directly")
public class ThreadRunProcessor extends SoraldAbstractProcessor<CtInvocation> {
    @Override
    protected boolean canRepairInternal(CtInvocation candidate) {
        return candidate.getExecutable().getSignature().equals("run()")
                && candidate.getExecutable().getDeclaringType().toString().equals("java.lang.Thread");
    }

    @Override
    protected void repairInternal(CtInvocation element) {
        Factory factory = element.getFactory();
        CtClass<?> threadClass = factory.Class().get(Thread.class);
        CtTypeAccess<?> access = factory.createTypeAccess(threadClass.getReference());

        CtMethod<?> method = threadClass.getMethodsByName("start").get(0);

        CtInvocation threadStartInvocation = factory.createInvocation(access, method.getReference());

        element.replace(threadStartInvocation);
    }
}
