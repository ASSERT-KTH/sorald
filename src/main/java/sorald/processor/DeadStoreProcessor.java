package sorald.processor;

import org.sonar.java.checks.DeadStoreCheck;
import org.sonar.plugins.java.api.JavaFileScanner;
import sorald.ProcessorAnnotation;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;

@ProcessorAnnotation(key = 1854, description = "Unused assignments should be removed")
public class DeadStoreProcessor extends SoraldAbstractProcessor<CtStatement> {

	public DeadStoreProcessor(){}

	@Override
	public JavaFileScanner getSonarCheck() {
		return new DeadStoreCheck();
	}

	@Override
	public boolean isToBeProcessed(CtStatement element) {
		if (!super.isToBeProcessedAccordingToStandards(element)) {
			return false;
		}
		if (element instanceof CtLocalVariable || element instanceof CtAssignment) {
			return true;
		}
		return false;
	}

	@Override
	public void process(CtStatement element) {
		super.process(element);
		element.delete();
	}

}
