package sorald.processor;

import org.sonar.java.checks.SelfAssignementCheck;

import org.sonar.plugins.java.api.JavaFileScanner;
import sorald.ProcessorAnnotation;
import spoon.reflect.factory.Factory;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtArrayAccess;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.declaration.CtType;

@ProcessorAnnotation(key = 1656, description = "Variables should not be self-assigned")
public class SelfAssignementProcessor extends SoraldAbstractProcessor<CtAssignment<?,?>> {

	public SelfAssignementProcessor(String originalFilesPath) {
		super(originalFilesPath);
	}

	@Override
	public JavaFileScanner getSonarCheck() {
		return new SelfAssignementCheck();
	}

	@Override
	public boolean isToBeProcessed(CtAssignment<?,?> candidate) {
		if (!super.isToBeProcessedAccordingToStandards(candidate)) {
			return false;
		}

		CtExpression<?> leftExpression = candidate.getAssigned();
		CtExpression<?> rightExpression = candidate.getAssignment();
		if (rightExpression == null || candidate.getParent(CtAssignment.class) != null) {
			/* Ignore multiple assignment case*/
			return false;
		}

		if (leftExpression.toString().equals(rightExpression.toString())) {
			return true;
		}

		return true;
	}

	@Override	
	public void process(CtAssignment<?,?> element) {
		super.process(element);

		Factory factory = element.getFactory();
		CtType<?> type = element.getParent(CtType.class);

		CtThisAccess access = factory.createThisAccess(type.getReference());
		CtFieldRead<?> fieldRead = factory.createFieldRead();

		fieldRead.setTarget(access);
		CtExpression<?> leftExpression = element.getAssigned();
		CtExpression<?> rightExpression = element.getAssignment();
		CtExpression<?> leftExpression2Check;
		CtExpression<?> rightExpression2Check;
		if (leftExpression instanceof CtArrayAccess && rightExpression instanceof CtArrayAccess) {
			leftExpression2Check = ((CtArrayAccess)leftExpression).getTarget();
			rightExpression2Check = ((CtArrayAccess)rightExpression).getTarget();
		} else {
			leftExpression2Check = leftExpression;
			rightExpression2Check = rightExpression;
		}
		boolean instanceOfFieldAccess = leftExpression2Check instanceof CtFieldAccess && rightExpression2Check instanceof CtFieldAccess; // True if no identical local variable present
		boolean instanceOfVariableAccess = leftExpression2Check instanceof CtVariableAccess && rightExpression2Check instanceof CtVariableAccess;

		if (instanceOfFieldAccess) {
			element.delete();
		} else if (!instanceOfFieldAccess && instanceOfVariableAccess) {
			CtField<?> field = type.getField(leftExpression2Check.toString());
			if (field != null) {
				fieldRead.setVariable(((CtVariable)field).getReference());
				leftExpression2Check.replace(fieldRead);
			} else {
				element.delete();
			}
		}
	}
}
