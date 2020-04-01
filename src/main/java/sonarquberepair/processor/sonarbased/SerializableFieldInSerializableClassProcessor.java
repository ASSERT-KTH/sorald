package sonarquberepair.processor.sonarbased;

import spoon.reflect.code.CtComment;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.ModifierKind;

import java.util.List;

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
		List<CtComment> comments = element.getComments();
		CtComment sp = null;
		for (CtComment comment : comments) {
			if (comment.getContent().indexOf("Noncompliant") != -1) {
				sp = comment;
			}
		}
		if (sp != null) {
			element.removeComment(sp);
		}
	}

}
