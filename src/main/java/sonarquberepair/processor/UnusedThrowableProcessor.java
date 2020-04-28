package sonarquberepair.processor;

import org.sonar.java.checks.unused.UnusedThrowableCheck;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtThrow;

public class UnusedThrowableProcessor extends SQRAbstractProcessor<CtConstructorCall> {

	public UnusedThrowableProcessor(String originalFilesPath) {
		super(originalFilesPath, new UnusedThrowableCheck());
	}

	@Override
	public boolean isToBeProcessed(CtConstructorCall element) {
		if (!super.isToBeProcessedAccordingToSonar(element)) {
			return false;
		}
		return true;
	}

	@Override
	public void process(CtConstructorCall element) {
		super.process(element);

		CtThrow ctThrow = getFactory().createCtThrow(element.toString());
		element.replace(ctThrow);
	}

}
