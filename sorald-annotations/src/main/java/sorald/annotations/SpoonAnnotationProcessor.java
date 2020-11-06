package sorald.annotations;

import spoon.processing.AbstractAnnotationProcessor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtCompilationUnit;

public class SpoonAnnotationProcessor extends AbstractAnnotationProcessor<ProcessorAnnotation, CtClass<?>> {
    private CtCompilationUnit cu = null;
    private CtClass<?> processorsClass = null;

    @Override
    public void process(ProcessorAnnotation annotation, CtClass<?> element) {
        if (cu == null)
            firstTimeSetup();

        cu.getImports().add(getFactory().createImport(element.getReference()));
    }

    public void firstTimeSetup() {
        cu = getFactory().createCompilationUnit();
        processorsClass = getFactory().createClass("sorald.Processors");
        cu.addDeclaredType(processorsClass);
    }

}
