package sonarquberepair.processor;

import org.sonar.java.checks.serialization.SerializableFieldInSerializableClassCheck;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.ModifierKind;

public class SerializableFieldInSerializableClassProcessor extends SQRAbstractProcessor<CtField> {

	public SerializableFieldInSerializableClassProcessor(String originalFilesPath) {
		super(originalFilesPath, new SerializableFieldInSerializableClassCheck());
	}

	@Override
	public boolean isToBeProcessed(CtField element) {
		if (!super.isToBeProcessedAccordingToSonar(element)) {
			return false;
		}
		return true;
	}

	@Override
	public void process(CtField element) {
		super.process(element);
		element.addModifier(ModifierKind.TRANSIENT);
	}

}
