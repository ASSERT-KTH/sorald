package sorald.processor;

import org.sonar.java.checks.CompareStringsBoxedTypesWithEqualsCheck;
import org.sonar.plugins.java.api.JavaFileScanner;
import sorald.ProcessorAnnotation;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.reference.CtTypeReference;

@ProcessorAnnotation(key = 4973, description = "Strings and Boxed types should be compared using \"equals()\"")
public class CompareStringsBoxedTypesWithEqualsProcessor extends SoraldAbstractProcessor<CtElement> {

	public CompareStringsBoxedTypesWithEqualsProcessor(String originalFilesPath) {
		super(originalFilesPath);
	}

	@Override
	public JavaFileScanner getSonarCheck() {
		return new CompareStringsBoxedTypesWithEqualsCheck();
	}

	@Override
	public boolean isToBeProcessed(CtElement candidate) {
		if (!super.isToBeProcessedAccordingToStandards(candidate)) {
			return false;
		}
		if (candidate instanceof CtBinaryOperator) {
			CtBinaryOperator op = (CtBinaryOperator) candidate;
			if (op.getKind() == BinaryOperatorKind.EQ || op.getKind() == BinaryOperatorKind.NE) {
				CtExpression left = op.getLeftHandOperand();
				CtExpression right = op.getRightHandOperand();
				CtTypeReference lType = left.getType();
				CtTypeReference rType = right.getType();
				/*
				The reason we don't check for the case where one variable is boxed is because Java implicitly unboxes
				the boxed type to primitive, making the == check fine. See JLS #5.6.2:
				https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.6.2
				*/
				CtTypeReference stringType = getFactory().Type().STRING;

				/*
				Case 1: Both variables are strings.
				Case 2: The left variable is a string and the right one is boxed.
				Case 3: The left variable is boxed and the right one is a string.
				Case 4: Both variables are boxed.
				*/
				if ((lType != null && rType != null) &&
					((lType.equals(stringType) && rType.equals(stringType)) ||
						(lType.equals(stringType) && !rType.unbox().equals(rType)) ||
						(!lType.unbox().equals(lType) && rType.equals(stringType)) ||
						(!lType.unbox().equals(lType) && !rType.unbox().equals(rType)))) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void process(CtElement element) {
		super.process(element);

		CtBinaryOperator bo = (CtBinaryOperator) element;
		String negation = "";
		if (((CtBinaryOperator) element).getKind() == BinaryOperatorKind.NE) {
			negation = "!";
		}
		CtCodeSnippetExpression newBinaryOperator = getFactory().Code().createCodeSnippetExpression(
			negation + bo.getLeftHandOperand().toString() + ".equals(" + bo.getRightHandOperand().toString() + ")");
		bo.replace(newBinaryOperator);
	}

}
