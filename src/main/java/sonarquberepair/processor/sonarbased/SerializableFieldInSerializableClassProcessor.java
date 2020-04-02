package sonarquberepair.processor.sonarbased;

import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.ModifierKind;

public class SerializableFieldInSerializableClassProcessor extends SonarWebAPIBasedProcessor<CtField> {

	public SerializableFieldInSerializableClassProcessor(String projectKey) {
		super(1948, projectKey);
	}

	@Override
	public boolean isToBeProcessed(CtField element) {
		if (element == null) {
			return false;
		}
		return super.isToBeProcessedAccordingToSonar(element);
	}

	@Override
	public void process(CtField element) {
		element.addModifier(ModifierKind.TRANSIENT);
	}

}
