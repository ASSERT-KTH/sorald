package sorald.annotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import spoon.processing.AbstractAnnotationProcessor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtCompilationUnit;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtWildcardReference;

public class SpoonAnnotationProcessor<T>
        extends AbstractAnnotationProcessor<ProcessorAnnotation, CtClass<T>> {
    private CtCompilationUnit cu = null;
    private CtClass<?> processorsClass = null;
    private CtType<?> soraldAbstractProcessor = null;
    private Map<Integer, CtClass<?>> processorMap = new HashMap<>();
    private String ruleDescriptions = "";

    private static final Set<ModifierKind> PUBLIC_STATIC_FINAL =
            new HashSet<>(
                    Arrays.asList(ModifierKind.PUBLIC, ModifierKind.STATIC, ModifierKind.FINAL));
    private static final Set<ModifierKind> PRIVATE_STATIC_FINAL =
            new HashSet<>(
                    Arrays.asList(ModifierKind.PRIVATE, ModifierKind.STATIC, ModifierKind.FINAL));

    @Override
    public void process(ProcessorAnnotation annotation, CtClass<T> element) {
        if (cu == null) firstTimeSetup();

        processorMap.put(annotation.key(), element);
        updateProcessorMapField();
        updateRuleDescriptions(annotation);

        cu.getImports().add(getFactory().createImport(element.getReference()));
    }

    private void updateProcessorMapField() {
        CtField procMapField = processorsClass.getField("RULE_KEY_TO_PROCESSOR");
        procMapField.setDefaultExpression(generateProductionProcessorMapInitializer());
    }

    private void updateRuleDescriptions(ProcessorAnnotation annotation) {
        ruleDescriptions += "\n" + annotation.key() + ": " + annotation.description();
        CtField field = processorsClass.getField("RULE_DESCRIPTIONS");
        field.setDefaultExpression(getFactory().createLiteral(ruleDescriptions));
    }

    private CtExpression<?> generateProductionProcessorMapInitializer() {
        String mapInitializer =
                "new java.util.HashMap() {{\n"
                        + processorMap.entrySet().stream()
                                .map(
                                        entry ->
                                                "put("
                                                        + entry.getKey()
                                                        + ","
                                                        + entry.getValue().getQualifiedName()
                                                        + ".class);")
                                .collect(Collectors.joining("\n"))
                        + "\n}}\n";
        return getFactory().createCodeSnippetExpression(mapInitializer);
    }

    private void firstTimeSetup() {
        Factory fact = getFactory();

        cu = getFactory().createCompilationUnit();
        processorsClass = fact.createClass("sorald.Processors");
        soraldAbstractProcessor = fact.Type().get("sorald.processor.SoraldAbstractProcessor");

        cu.addDeclaredType(processorsClass);

        addGetRuleDescriptions(processorsClass);
        addGetProcessor(processorsClass);

        CtTypeReference<?> mapTypeRef = getFactory().createCtTypeReference(Map.class);
        mapTypeRef.addActualTypeArgument(getFactory().Type().INTEGER);
        mapTypeRef.addActualTypeArgument(createClassTypeRefWithUpperBound(soraldAbstractProcessor));
    }

    private void addGetRuleDescriptions(CtType<?> type) {
        Factory fact = getFactory();
        CtMethod<String> getRuleDescriptions =
                fact.createMethod(
                        processorsClass,
                        PUBLIC_STATIC_FINAL,
                        fact.Type().STRING,
                        "getRuleDescriptions",
                        Collections.emptyList(),
                        Collections.emptySet());
        CtReturn<String> retStatement = fact.createReturn();
        retStatement.setReturnedExpression(fact.createLiteral(""));
        getRuleDescriptions.setBody(fact.createCtBlock(retStatement));
    }

    private void addGetProcessor(CtType<?> type) {
        Factory fact = getFactory();
        Set<ModifierKind> publicStaticFinal =
                new HashSet<>(
                        Arrays.asList(
                                ModifierKind.PUBLIC, ModifierKind.STATIC, ModifierKind.FINAL));

        CtTypeReference<?> cls = fact.createCtTypeReference(Class.class);
        CtMethod<String> getProcessor =
                fact.createMethod(
                        processorsClass,
                        publicStaticFinal,
                        cls,
                        "getProcessor",
                        Collections.emptyList(),
                        Collections.emptySet());
        fact.createParameter(getProcessor, fact.Type().INTEGER_PRIMITIVE, "key");

        CtReturn<String> retStatement = fact.createReturn();
        retStatement.setReturnedExpression(
                fact.createCodeSnippetExpression("processorMap.get(key)"));
        getProcessor.setBody(fact.createCtBlock(retStatement));
    }

    private CtTypeReference<?> createClassTypeRefWithUpperBound(CtType<?> upperBound) {
        CtTypeReference<?> clsWithBound = getFactory().Type().get(Class.class).getReference();
        CtWildcardReference wildcard = getFactory().createWildcardReference();
        wildcard.setBoundingType(upperBound.getReference());
        wildcard.setUpper(true);
        clsWithBound.addActualTypeArgument(wildcard);
        return clsWithBound;
    }
}
