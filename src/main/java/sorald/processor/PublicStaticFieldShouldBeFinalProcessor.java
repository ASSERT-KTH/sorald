package sorald.processor;

import java.util.Set;
import org.sonar.java.checks.PublicStaticFieldShouldBeFinalCheck;
import org.sonar.plugins.java.api.JavaFileScanner;
import sorald.ProcessorAnnotation;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.ModifierKind;

@ProcessorAnnotation(key = 1444, description = "\"public static\" fields should be constant")
public class PublicStaticFieldShouldBeFinalProcessor extends SoraldAbstractProcessor<CtField<?>> {
    @Override
    public JavaFileScanner getSonarCheck() {
        return new PublicStaticFieldShouldBeFinalCheck();
    }

    @Override
    public boolean isToBeProcessed(CtField<?> candidate) {
        if (!super.isToBeProcessedAccordingToStandards(candidate)) {
            return false;
        }
        Set<ModifierKind> modifiers = candidate.getModifiers();
        return modifiers.contains(ModifierKind.PUBLIC) && !modifiers.contains(ModifierKind.FINAL);
    }

    @Override
    public void process(CtField<?> element) {
        super.process(element);
        element.addModifier(ModifierKind.FINAL);
    }
}
