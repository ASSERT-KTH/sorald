package sonarquberepair.processor;

import org.sonar.java.checks.DeadStoreCheck;
import sonarquberepair.ProcessorAnnotation;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;

@ProcessorAnnotation(description="Unused assignments should be removed")
public class DeadStoreProcessor extends SQRAbstractProcessor<CtStatement> {

	public DeadStoreProcessor(String originalFilesPath) {
		super(originalFilesPath, new DeadStoreCheck());
	}

	@Override
	public boolean isToBeProcessed(CtStatement element) {
		if (!super.isToBeProcessedAccordingToSonar(element)) {
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
