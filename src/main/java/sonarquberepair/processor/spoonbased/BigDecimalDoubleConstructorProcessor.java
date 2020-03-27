package sonarquberepair.processor.spoonbased;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.List;
import java.math.BigDecimal;

public class BigDecimalDoubleConstructorProcessor extends AbstractProcessor<CtConstructorCall> {

	@Override
	public boolean isToBeProcessed(CtConstructorCall cons) {
		CtTypeReference bigDecimalTypeRef = getFactory().createCtTypeReference(BigDecimal.class);
		CtTypeReference doubleTypeRef = getFactory().createCtTypeReference(double.class);
		CtTypeReference floatTypeRef = getFactory().createCtTypeReference(float.class);

		if (cons.getType().equals(bigDecimalTypeRef)) {
			List<CtExpression> expr = cons.getArguments();
			if (expr.size() == 1 &&
					(expr.get(0).getType().equals(doubleTypeRef) || expr.get(0).getType().equals(floatTypeRef))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void process(CtConstructorCall cons) {
		CtType bigDecimalClass = getFactory().Class().get(BigDecimal.class);
		CtCodeSnippetExpression invoker = getFactory().Code().createCodeSnippetExpression("BigDecimal");
		CtMethod valueOfMethod = (CtMethod) bigDecimalClass.getMethodsByName("valueOf").get(0);
		CtExecutableReference refToMethod = getFactory().Executable().createReference(valueOfMethod);
		CtExpression arg = (CtExpression) cons.getArguments().get(0);
		CtInvocation newInvocation = getFactory().Code().createInvocation(invoker, refToMethod, arg);
		cons.replace(newInvocation);
	}
}
