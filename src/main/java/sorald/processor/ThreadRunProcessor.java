package sorald.processor;

import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

@ProcessorAnnotation(key = 1217, description = "\"Thread.run()\" should not be called directly")
public class ThreadRunProcessor extends SoraldAbstractProcessor<CtInvocation<?>> {
    @Override
    protected boolean canRepairInternal(CtInvocation<?> candidate) {
        return candidate.getExecutable() != null
                && candidate.getExecutable().getDeclaringType() != null
                && candidate.getExecutable().getSignature().equals("run()")
                && candidate
                        .getExecutable()
                        .getDeclaringType()
                        .toString()
                        .equals("java.lang.Thread");
    }

    @Override
    protected void repairInternal(CtInvocation<?> element) {
        Factory factory = element.getFactory();
        CtClass<?> threadClass = factory.Class().get(Thread.class);

        CtMethod<?> method = threadClass.getMethodsByName("start").get(0);

        CtInvocation<?> threadStartInvocation =
                factory.createInvocation(element.getTarget(), method.getReference());

        element.replace(threadStartInvocation);
    }
}
