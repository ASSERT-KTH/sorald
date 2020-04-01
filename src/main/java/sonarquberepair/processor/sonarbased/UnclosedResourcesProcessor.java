package sonarquberepair.processor.sonarbased;

import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtTry;
import spoon.reflect.code.CtTryWithResource;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtVariableReference;

public class UnclosedResourcesProcessor extends SonarWebAPIBasedProcessor<CtConstructorCall> {

	String var; //contains name of resource which is unclosed in the current bug.

	public UnclosedResourcesProcessor(String projectKey) {
		super(2095, projectKey);
	}

	@Override
	public boolean isToBeProcessed(CtConstructorCall element) {
		if (element == null) {
			return false;
		}
		return super.isToBeProcessedAccordingToSonar(element);
	}

	@Override
	public void process(CtConstructorCall element) {
		System.out.println("BUG\n");
		CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();
		final String value = String.format("[Spoon inserted try-with-resource],\n Repairs sonarqube rule 2095:\n %s should be closed", var);
		snippet.setValue(value);
		CtComment comment = getFactory().createComment(value, CtComment.CommentType.BLOCK);

		CtElement parent = element.getParent(e -> e instanceof CtAssignment || e instanceof CtLocalVariable);

		System.out.println(element + " " + element.getPosition());
		boolean isInTry = parent.getParent(CtBlock.class).getParent() instanceof CtTry;
		if (parent instanceof CtLocalVariable) {
			CtLocalVariable variable = ((CtLocalVariable) parent).clone();

			CtBlock block = parent.getParent(CtBlock.class);
			parent.delete();

			CtTryWithResource tryWithResource = getFactory().createTryWithResource();
			tryWithResource.addResource(variable);
			tryWithResource.addComment(comment);
			CtBlock bb = getFactory().createCtBlock(tryWithResource);
			if (isInTry) {
				block.getParent().replace(bb);
			} else {
				block.replace(bb);
			}
			tryWithResource.setBody(block);
		} else if (parent instanceof CtAssignment) {
			CtAssignment assign = (CtAssignment) parent;
			CtExpression expr = assign.getAssigned();

			if (expr instanceof CtVariableWrite) {
				CtVariableWrite variableWrite = (CtVariableWrite) expr;
				CtVariableReference variableReference = variableWrite.getVariable();
				if (variableReference.getDeclaration() instanceof CtLocalVariable) {
					CtLocalVariable var = (CtLocalVariable) variableReference.getDeclaration();
					CtLocalVariable variable = var.clone();
					variable.setAssignment(assign.getAssignment().clone());
					CtBlock block = parent.getParent(CtBlock.class);

					parent.delete();
					var.delete();

					CtTryWithResource tryWithResource = getFactory().createTryWithResource();
					tryWithResource.addResource(variable);
					tryWithResource.addComment(comment);

					CtBlock bb = getFactory().createCtBlock(tryWithResource);
					if (isInTry) {
						block.getParent().replace(tryWithResource);
					} else {
						block.replace(bb);
					}
					tryWithResource.setBody(block);
				}
			}
		}
	}

}
