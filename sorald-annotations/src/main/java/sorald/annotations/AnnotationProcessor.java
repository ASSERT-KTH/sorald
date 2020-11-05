package sorald.annotations;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@SupportedAnnotationTypes("sorald.ProcessorAnnotation")
public class AnnotationProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements =
                    roundEnv.getElementsAnnotatedWith(annotation);
            annotatedElements.stream()
                    .filter(e -> e.getKind() == ElementKind.CLASS)
                    .map(e -> (javax.lang.model.type.DeclaredType) e.asType())
                    .forEach(type -> System.out.println(type.getTypeArguments()));
        }
        return false;
    }
}
