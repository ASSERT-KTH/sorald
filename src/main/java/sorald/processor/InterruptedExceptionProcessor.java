package sorald.processor;

import org.sonar.java.checks.InterruptedExceptionCheck;
import sorald.ProcessorAnnotation;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

@ProcessorAnnotation(key = 2142, description = "\"InterruptedException\" should not be ignored")
public class InterruptedExceptionProcessor extends SoraldAbstractProcessor<CtCatch> {

	public InterruptedExceptionProcessor(String originalFilesPath) {
		super(originalFilesPath, new InterruptedExceptionCheck());
	}

	@Override
	public boolean isToBeProcessed(CtCatch candidate) {
		if (!super.isToBeProcessedAccordingToStandards(candidate)) {
			return false;
		}
		return true;
	}

	@Override
	public void process(CtCatch element) {
		super.process(element);

		Factory factory = element.getFactory();
		CtClass<?> threadClass = factory.Class().get(Thread.class);
		CtTypeAccess<?> threadClassAccess = factory.createTypeAccess(threadClass.getReference());
		CtMethod<?> currentThreadMethod = threadClass.getMethodsByName("currentThread").get(0);
		CtMethod<?> interruptMethod = threadClass.getMethodsByName("interrupt").get(0);
		CtInvocation firstInvocation = factory.createInvocation(threadClassAccess, currentThreadMethod.getReference());
		CtInvocation secondInvocation = factory.createInvocation(firstInvocation, interruptMethod.getReference());

		element.getBody().addStatement(element.getBody().getStatements().size(), secondInvocation);
	}

}
