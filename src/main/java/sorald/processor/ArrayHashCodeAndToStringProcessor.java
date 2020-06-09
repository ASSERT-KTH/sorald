package sorald.processor;

import org.sonar.java.checks.ArrayHashCodeAndToStringCheck;
import sorald.Constants;
import sorald.ProcessorAnnotation;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;

import java.util.Arrays;

@ProcessorAnnotation(key = 2116, description = "\"hashCode\" and \"toString\" should not be called on array instances")
public class ArrayHashCodeAndToStringProcessor extends SoraldAbstractProcessor<CtInvocation<?>> {

	public ArrayHashCodeAndToStringProcessor(String originalFilesPath) {
		super(originalFilesPath, new ArrayHashCodeAndToStringCheck());
	}

	@Override
	public boolean isToBeProcessed(CtInvocation<?> candidate) {
		if (!super.isToBeProcessedAccordingToSonar(candidate)) {
			return false;
		}
		if (candidate.getTarget() == null) {
			return false;
		}
		if (candidate.getTarget().getType().isArray()) {
			if (candidate.getExecutable().getSignature().equals(Constants.TOSTRING_METHOD_NAME + "()") ||
				(candidate.getExecutable().getSignature().equals(Constants.HASHCODE_METHOD_NAME + "()"))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void process(CtInvocation<?> element) {
		super.process(element);

		CtExpression prevTarget = element.getTarget();
		CtClass arraysClass = getFactory().Class().get(Arrays.class);
		CtTypeAccess<?> newTarget = getFactory().createTypeAccess(arraysClass.getReference());
		CtMethod method = null;
		if (element.getExecutable().getSignature().equals(Constants.HASHCODE_METHOD_NAME + "()")) {
			method = (CtMethod) arraysClass.getMethodsByName(Constants.HASHCODE_METHOD_NAME).get(0);
		} else if (element.getExecutable().getSignature().equals(Constants.TOSTRING_METHOD_NAME + "()")) {
			method = (CtMethod) arraysClass.getMethodsByName(Constants.TOSTRING_METHOD_NAME).get(0);
		} else {
			System.err.println("Unhandled case. Something went wrong.");
		}
		CtExecutableReference refToMethod = getFactory().Executable().createReference(method);
		CtInvocation newInvocation = getFactory().Code().createInvocation(newTarget, refToMethod, prevTarget);
		element.replace(newInvocation);
	}

}
