package sonarquberepair.processor.spoonbased;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import sonarquberepair.processor.SQRAbstractProcessor;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtExecutableReference;

public class EqualsOnAtomicClassProcessor extends SQRAbstractProcessor<CtInvocation> {

	@Override
	public boolean isToBeProcessed(CtInvocation candidate) {
		if (candidate.getExecutable().getSignature().equals("equals(java.lang.Object)") &&
				isAtomicClassRef(candidate.getTarget())) {
			return true;
		}
		return false;
	}

	@Override
	public void process(CtInvocation element) {
		super.process(element);

		CtType atomicClass;
		if (isAtomicInteger(element.getTarget())) {
			atomicClass = getFactory().Class().get(AtomicInteger.class);
		} else if (isAtomicLong(element.getTarget())) {
			atomicClass = getFactory().Class().get(AtomicLong.class);
		} else {
			atomicClass = getFactory().Class().get(AtomicBoolean.class);
		}

		CtMethod ctMethodToBeCalled = (CtMethod) atomicClass.getMethodsByName("get").get(0);
		CtExecutableReference ctExecutableReferenceToMethodToBeCalled = getFactory().Executable().createReference(ctMethodToBeCalled);

		CtInvocation leftInvocation = getFactory().Code().createInvocation(element.getTarget(), ctExecutableReferenceToMethodToBeCalled);
		CtInvocation rightInvocation = getFactory().Code().createInvocation((CtExpression) element.getArguments().get(0), ctExecutableReferenceToMethodToBeCalled);

		CtBinaryOperator newCtBinaryOperator = getFactory().Code().createBinaryOperator(leftInvocation, rightInvocation, BinaryOperatorKind.EQ);

		element.replace(newCtBinaryOperator);
	}

	private boolean isAtomicClassRef(CtExpression ctExpression) {
		return isAtomicInteger(ctExpression) || isAtomicLong(ctExpression) || isAtomicBoolean(ctExpression);
	}

	private boolean isAtomicInteger(CtExpression ctExpression) {
		return ctExpression.getType().getQualifiedName().equals("java.util.concurrent.atomic.AtomicInteger");
	}

	private boolean isAtomicLong(CtExpression ctExpression) {
		return ctExpression.getType().getQualifiedName().equals("java.util.concurrent.atomic.AtomicLong");
	}

	private boolean isAtomicBoolean(CtExpression ctExpression) {
		return ctExpression.getType().getQualifiedName().equals("java.util.concurrent.atomic.AtomicBoolean");
	}

}
