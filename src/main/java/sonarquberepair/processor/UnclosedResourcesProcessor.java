package sonarquberepair.processor;

import org.sonar.java.se.checks.UnclosedResourcesCheck;
import sonarquberepair.ProcessorAnnotation;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtTry;
import spoon.reflect.code.CtTryWithResource;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.path.CtRole;
import spoon.reflect.reference.CtVariableReference;

@ProcessorAnnotation(key = 2095, description = "Resources should be closed")
public class UnclosedResourcesProcessor extends SQRAbstractProcessor<CtConstructorCall> {

	public UnclosedResourcesProcessor(String originalFilesPath) {
		super(originalFilesPath, new UnclosedResourcesCheck());
	}

	@Override
	public boolean isToBeProcessed(CtConstructorCall element) {
		if (!super.isToBeProcessedAccordingToStandards(element)) {
			return false;
		}
		CtElement parent = element.getParent(e -> e instanceof CtAssignment || e instanceof CtLocalVariable);
		if (parent != null && parent.getRoleInParent() == CtRole.TRY_RESOURCE) {
			return false;
		}
		return true;
	}

	@Override
	public void process(CtConstructorCall element) {
		super.process(element);

		CtElement parent = element.getParent(e -> e instanceof CtAssignment || e instanceof CtLocalVariable);

		if (parent instanceof CtLocalVariable) {
			CtLocalVariable ctLocalVariable = ((CtLocalVariable) parent);
			createCtTryWithResource(parent, ctLocalVariable.clone());
		} else if (parent instanceof CtAssignment) {
			CtAssignment ctAssignment = (CtAssignment) parent;
			CtExpression expressionAssigned = ctAssignment.getAssigned();

			if (expressionAssigned instanceof CtVariableWrite) {
				CtVariableWrite ctVariableWrite = (CtVariableWrite) expressionAssigned;
				CtVariableReference ctVariableReference = ctVariableWrite.getVariable();
				if (ctVariableReference.getDeclaration() instanceof CtLocalVariable) {
					CtLocalVariable ctLocalVariable = (CtLocalVariable) ctVariableReference.getDeclaration();
					CtLocalVariable clonedCtLocalVariable = ctLocalVariable.clone();
					clonedCtLocalVariable.setAssignment(ctAssignment.getAssignment().clone());
					ctLocalVariable.delete();
					createCtTryWithResource(parent, clonedCtLocalVariable);
				}
			}
		}
	}

	private void createCtTryWithResource(CtElement parent, CtLocalVariable variable) {
		CtTryWithResource tryWithResource = getFactory().createTryWithResource();
		tryWithResource.addResource(variable);

		CtBlock parentCtBlock = parent.getParent(CtBlock.class);
		boolean isInTry = parentCtBlock.getParent() instanceof CtTry;
		if (isInTry) {
			parent.delete();
			tryWithResource.setCatchers(((CtTry) parentCtBlock.getParent()).getCatchers());
			parentCtBlock.getParent().replace(tryWithResource);
			tryWithResource.setBody(parentCtBlock);
		} else {
			CtBlock newCtBlock = null;
			int indexOfTheFirstStatementToBeDeleted = -1;

			for (int i = 0; i < parentCtBlock.getStatements().size(); i++) {
				CtStatement ctStatement = parentCtBlock.getStatements().get(i);
				if (ctStatement.equals(parent)) {
					indexOfTheFirstStatementToBeDeleted = i + 1;
					continue;
				}
				if (indexOfTheFirstStatementToBeDeleted != -1) {
					if (newCtBlock == null) {
						newCtBlock = getFactory().createCtBlock(ctStatement.clone());
					} else {
						newCtBlock.addStatement(ctStatement.clone());
					}
				}
			}
			int nbOfStatements = parentCtBlock.getStatements().size();
			if (indexOfTheFirstStatementToBeDeleted != -1) {
				for (int i = 0; i < (nbOfStatements - indexOfTheFirstStatementToBeDeleted); i++) {
					parentCtBlock.getStatement(indexOfTheFirstStatementToBeDeleted).delete();
				}
			}

			tryWithResource.setBody(newCtBlock);
			parent.replace(tryWithResource);
		}

	}

}
