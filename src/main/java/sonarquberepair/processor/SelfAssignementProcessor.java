package sonarquberepair.processor;

import org.sonar.java.checks.SelfAssignementCheck;

import spoon.reflect.factory.Factory;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtArrayAccess;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtTargetedExpression;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.declaration.CtType;

import java.util.ArrayList;
import java.util.List;


public class SelfAssignementProcessor extends SQRAbstractProcessor<CtAssignment<?,?>> {

	public SelfAssignementProcessor(String originalFilesPath) {
		super(originalFilesPath, new SelfAssignementCheck());
	}

	@Override
	public boolean isToBeProcessed(CtAssignment<?,?> candidate) {
		if (!super.isToBeProcessedAccordingToSonar(candidate)) {
			return false;
		}

		CtExpression<?> leftExpression = candidate.getAssigned();
		CtExpression<?> rightExpression = candidate.getAssignment();
		if (rightExpression == null || candidate.getParent(CtAssignment.class) != null) {
			/* Ignore multiple assigment case*/
			return false;
		}

		if (leftExpression.toString().equals(rightExpression.toString())) {
			if (!candidate.getAssigned().toString().contains("this.") && candidate.getAssigned().toString().contains(".")) {
				return false;
			}
			return true;
		}
		return false;
	}

	@Override	
	public void process(CtAssignment<?,?> element) {
		Factory factory = element.getFactory();
		CtType<?> type = element.getParent(CtType.class);

		CtThisAccess access = factory.createThisAccess(type.getReference());
		CtFieldRead<?> fieldRead = factory.createFieldRead();

		fieldRead.setTarget(access);

		CtExpression<?> leftExpression = element.getAssigned();
		CtExpression<?> rightExpression = element.getAssignment();

		if (leftExpression instanceof CtFieldAccess && rightExpression instanceof CtFieldAccess) {
			element.delete();
		} else if (leftExpression instanceof CtVariableAccess && rightExpression instanceof CtVariableAccess) {
			CtField<?> field = type.getField(leftExpression.toString());
			if (field != null) {
				fieldRead.setVariable(((CtVariable)field).getReference());
				leftExpression.replace(fieldRead);
			} else {
				element.delete();
			}
		} else if (leftExpression instanceof CtArrayAccess && rightExpression instanceof CtArrayAccess) {
			CtField<?> field = type.getField(((CtArrayAccess)leftExpression).getTarget().toString());
			if (field != null) {
				CtTargetedExpression targetExpression = (CtTargetedExpression) ((CtArrayAccess)leftExpression).getTarget();
				targetExpression.setTarget(access);
			} else {
				element.delete();
			}
		}
	}
}
