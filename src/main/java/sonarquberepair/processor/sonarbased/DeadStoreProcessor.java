package sonarquberepair.processor.sonarbased;

import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;

public class DeadStoreProcessor extends SonarWebAPIBasedProcessor<CtStatement> {

	String var; //contains name of variable which is uselessly assigned in the current bug.

	public DeadStoreProcessor(String projectKey) {
		super(1854, projectKey);
	}

	@Override
	public boolean isToBeProcessed(CtStatement element) {
		if (element == null) {
			return false;
		}
		if (!(element instanceof CtLocalVariable) && !(element instanceof CtAssignment)) {
			return false;
		}
		return super.isToBeProcessedAccordingToSonar(element);
	}

	@Override
	public void process(CtStatement element) {
		System.out.println("BUG\n");
		CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();
		final String value = String.format("//[Spoon inserted check], repairs sonarqube rule 1854:Dead stores should be removed,\n//useless assignment to %s removed", var);
		snippet.setValue(value);
		element.delete();
	}

}
