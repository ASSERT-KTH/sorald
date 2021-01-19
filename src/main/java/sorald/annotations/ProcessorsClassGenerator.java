package sorald.annotations;

import java.util.SortedMap;
import java.util.TreeMap;
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
    private final SortedMap<Integer, CtClass<?>> processorMap = new TreeMap<>();
    private final SortedMap<Integer, String> descriptions = new TreeMap<>();

    @Override
    public void process(ProcessorAnnotation annotation, CtClass<T> element) {
        if (cu == null) {
            cu = getFactory().createCompilationUnit();
            processorsClass = getFactory().Type().get("sorald.Processors");
        }

        processorMap.put(annotation.key(), element);
        descriptions.put(
                annotation.key(),
                generateRuleDescription(
                        annotation, element.getAnnotation(IncompleteProcessor.class)));
        updateProcessorMapField();
        updateRuleDescriptions();
    }

    private void updateProcessorMapField() {
        CtField procMapField = processorsClass.getField("RULE_KEY_TO_PROCESSOR");
        procMapField.setDefaultExpression(generateProductionProcessorMapInitializer());
    }

    private void updateRuleDescriptions() {
        String ruleDescriptions = String.join("\n", descriptions.values());
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
