package sonarquberepair.processor.spoonbased;

import sonarquberepair.processor.SQRAbstractProcessor;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.code.CtReturn;
import spoon.reflect.factory.Factory;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.List;

public class CompareToReturnValueProcessor extends SQRAbstractProcessor<CtMethod<?>> {

	@Override
	public boolean isToBeProcessed(CtMethod<?> method) {
		String returnTypeName = method.getType().getSimpleName();
		if (method.getSimpleName().equals("compareTo") && (returnTypeName.equals("int") || returnTypeName.equals("Integer"))) {
			return true;
		}
		return false;
	}

	@Override
	public void process(CtMethod<?> method) {
		super.process(method);

		Factory factory = method.getFactory();
		CtLiteral<?> elem2Replace = factory.createLiteral(-1);

		List<CtReturn<?>> returns = method.getElements(new TypeFilter(CtReturn.class));
		for(CtReturn<?> elem : returns) {
			if (elem.getReturnedExpression().toString().indexOf("Integer.MIN_VALUE") != -1) {
				elem.getReturnedExpression().replace(elem2Replace);
			}
		}
	}

}
