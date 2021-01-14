package sorald.annotations;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import spoon.processing.AbstractAnnotationProcessor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtCompilationUnit;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtType;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ProcessorsClassGenerator<T>
        extends AbstractAnnotationProcessor<ProcessorAnnotation, CtClass<T>> {
    private CtCompilationUnit cu = null;
    private CtType<?> processorsClass = null;
    private final Map<Integer, CtClass<?>> processorMap = new HashMap<>();
    private String ruleDescriptions = "";

    @Override
    public void process(ProcessorAnnotation annotation, CtClass<T> element) {
        if (cu == null) {
            cu = getFactory().createCompilationUnit();
            processorsClass = getFactory().Type().get("sorald.Processors");
        }

        processorMap.put(annotation.key(), element);
        updateProcessorMapField();
        updateRuleDescriptions(annotation, element.getAnnotation(IncompleteProcessor.class));
    }

    private void updateProcessorMapField() {
        CtField procMapField = processorsClass.getField("RULE_KEY_TO_PROCESSOR");
        procMapField.setDefaultExpression(generateProductionProcessorMapInitializer());
    }

    private void updateRuleDescriptions(
            ProcessorAnnotation processorAnnotation, IncompleteProcessor incompleteAnnotation) {
        ruleDescriptions +=
                "\n" + generateRuleDescription(processorAnnotation, incompleteAnnotation);
        CtField<String> field = (CtField<String>) processorsClass.getField("RULE_DESCRIPTIONS");
        field.setDefaultExpression(getFactory().createLiteral(ruleDescriptions));
    }

    private CtExpression<?> generateProductionProcessorMapInitializer() {
        String mapInitializer =
                "new java.util.HashMap<>() {{\n"
                        + processorMap.entrySet().stream()
                                .map(
                                        entry ->
                                                "put("
                                                        + entry.getKey()
                                                        + ","
                                                        + entry.getValue().getSimpleName()
                                                        + ".class);")
                                .collect(Collectors.joining("\n"))
                        + "\n}}\n";
        return getFactory().createCodeSnippetExpression(mapInitializer);
    }

    private String generateRuleDescription(
            ProcessorAnnotation processorAnnotation, IncompleteProcessor incompleteAnnotation) {
        return processorAnnotation.key()
                + ": "
                + processorAnnotation.description()
                + (incompleteAnnotation == null
                        ? ""
                        : "\n\t(incomplete: " + incompleteAnnotation.description() + ")");
    }
}
