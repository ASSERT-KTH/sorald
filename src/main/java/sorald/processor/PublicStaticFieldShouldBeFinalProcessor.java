package sorald.processor;

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
        return super.isToBeProcessedAccordingToStandards(candidate);
    }

    @Override
    public void process(CtField<?> element) {
        super.process(element);
        element.addModifier(ModifierKind.FINAL);
    }
}
