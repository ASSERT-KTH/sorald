package sonarquberepair.processor;

import sonarquberepair.UniqueTypesCollector;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtElement;

/** superclass for all processors */
public abstract class SQRAbstractProcessor<E extends CtElement> extends AbstractProcessor<E> {

	@Override
	public void process(E element) {
		UniqueTypesCollector.getInstance().collect(element);
	}

}
