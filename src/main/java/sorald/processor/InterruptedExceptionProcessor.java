package sorald.processor;

import org.sonar.java.checks.InterruptedExceptionCheck;
import org.sonar.plugins.java.api.JavaFileScanner;
import sorald.ProcessorAnnotation;
import sorald.FileTreeAlgorithm.Node;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

import java.util.List;

@ProcessorAnnotation(key = 2142, description = "\"InterruptedException\" should not be ignored")
public class InterruptedExceptionProcessor extends SoraldAbstractProcessor<CtCatch> {

	public InterruptedExceptionProcessor(String originalFilesPath) {
		super(originalFilesPath);
	}

	@Override
	public JavaFileScanner getSonarCheck() {
		return new InterruptedExceptionCheck();
	}

	public InterruptedExceptionProcessor(List<Node> segments) throws Exception {
        super(segments);
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
