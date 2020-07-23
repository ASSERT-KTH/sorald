package sorald.processor;

import org.sonar.java.checks.CompareToReturnValueCheck;
import org.sonar.plugins.java.api.JavaFileScanner;
import sorald.Constants;
import sorald.ProcessorAnnotation;
import sorald.FileTreeAlgorithm.Node;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtLiteral;

import java.util.List;

@ProcessorAnnotation(key = 2167, description = "\"compareTo\" should not return \"Integer.MIN_VALUE\"")
public class CompareToReturnValueProcessor extends SoraldAbstractProcessor<CtReturn<?>> {

	public CompareToReturnValueProcessor(String originalFilesPath) {
		super(originalFilesPath);
	}

	@Override
	public JavaFileScanner getSonarCheck() {
		return new CompareToReturnValueCheck();
	}

	public CompareToReturnValueProcessor(List<Node> segment) {
        super(segment, new CompareToReturnValueCheck());
    }

	@Override
	public boolean isToBeProcessed(CtReturn<?> ctReturn) {
		if (!super.isToBeProcessedAccordingToStandards(ctReturn)) {
			return false;
		}
		CtMethod ctMethod = ctReturn.getParent(CtMethod.class);
		String returnTypeName = ctMethod.getType().getSimpleName();
		if (ctMethod.getSimpleName().equals("compareTo") && (returnTypeName.equals(Constants.INT) || returnTypeName.equals("Integer"))
				&& ctReturn.getReturnedExpression().toString().indexOf("Integer.MIN_VALUE") != -1) {
			return true;
		}
		return false;
	}

	@Override
	public void process(CtReturn<?> ctReturn) {
		super.process(ctReturn);

		CtLiteral<?> elem2Replace = ctReturn.getFactory().createLiteral(-1);
		ctReturn.getReturnedExpression().replace(elem2Replace);
	}

}
