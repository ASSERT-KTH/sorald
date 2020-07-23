package sorald.processor;

import org.sonar.java.checks.unused.UnusedThrowableCheck;
import org.sonar.plugins.java.api.JavaFileScanner;
import sorald.ProcessorAnnotation;
import sorald.FileTreeAlgorithm.Node;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtThrow;

import java.util.List;

@ProcessorAnnotation(key = 3984, description = "Exception should not be created without being thrown")
public class UnusedThrowableProcessor extends SoraldAbstractProcessor<CtConstructorCall> {

	public UnusedThrowableProcessor(String originalFilesPath) {
		super(originalFilesPath);
	}

	@Override
	public JavaFileScanner getSonarCheck() {
		return new UnusedThrowableCheck();
	}

	public UnusedThrowableProcessor(List<Node> segment) {
        super(segment, new UnusedThrowableCheck());
    }

	@Override
	public boolean isToBeProcessed(CtConstructorCall element) {
		if (!super.isToBeProcessedAccordingToStandards(element)) {
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
