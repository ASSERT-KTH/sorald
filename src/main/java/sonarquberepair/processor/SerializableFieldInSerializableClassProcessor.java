package sonarquberepair.processor;

import org.sonar.java.checks.serialization.SerializableFieldInSerializableClassCheck;
import sonarquberepair.ProcessorAnnotation;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.ModifierKind;

@ProcessorAnnotation(key = 1948, description = "Fields in a \"Serializable\" class should either be transient or serializable")
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
