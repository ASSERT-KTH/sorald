package sonarquberepair.processor.spoonbased;

import sonarquberepair.UniqueTypesCollector;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import java.math.BigDecimal;
import java.util.List;

public class BigDecimalDoubleConstructorProcessor extends AbstractProcessor<CtConstructorCall> {

	@Override
	public boolean isToBeProcessed(CtConstructorCall cons) {
		CtTypeReference bigDecimalTypeRef = getFactory().createCtTypeReference(BigDecimal.class);
		CtTypeReference doubleTypeRef = getFactory().createCtTypeReference(double.class);
		CtTypeReference floatTypeRef = getFactory().createCtTypeReference(float.class);

		if (cons.getType().equals(bigDecimalTypeRef)) {
			List<CtExpression> expr = cons.getArguments();
			if ((expr.size() == 1 || expr.size() == 2) &&
				(expr.get(0).getType().equals(doubleTypeRef) || expr.get(0).getType().equals(floatTypeRef))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void process(CtConstructorCall cons) {
		UniqueTypesCollector.getInstance().collect(cons);

		if (cons.getArguments().size() == 1) {
			CtType bigDecimalClass = getFactory().Class().get(BigDecimal.class);
			CtCodeSnippetExpression invoker = getFactory().Code().createCodeSnippetExpression("BigDecimal");
			CtMethod valueOfMethod = (CtMethod) bigDecimalClass.getMethodsByName("valueOf").get(0);
			CtExecutableReference refToMethod = getFactory().Executable().createReference(valueOfMethod);
			CtExpression arg = (CtExpression) cons.getArguments().get(0);
			CtInvocation newInvocation = getFactory().Code().createInvocation(invoker, refToMethod, arg);
			cons.replace(newInvocation);
		} else {
			CtConstructorCall newCtConstructorCall = cons.clone();
			CtExpression arg = (CtExpression) cons.getArguments().get(0);
			String argValue = arg.toString().replaceAll("[fFdD]", "");
			CtLiteral<String> literal = getFactory().Code().createLiteral(argValue);
			newCtConstructorCall.getArguments().set(0, literal);
			cons.replace(newCtConstructorCall);
		}
	}

}
