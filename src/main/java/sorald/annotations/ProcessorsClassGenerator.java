package sorald.annotations;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import spoon.Launcher;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtWildcardReference;

/**
 * Annotation processor that generates the class sorald.Processors, which contains utility methods
 * for fetching Sorald processors and their descriptions.
 */
@SupportedAnnotationTypes({
    "sorald.annotations.ProcessorAnnotation",
})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ProcessorsClassGenerator extends AbstractProcessor {
    private static final Set<ModifierKind> PUBLIC_STATIC_FINAL =
            new HashSet<>(
                    Arrays.asList(ModifierKind.PUBLIC, ModifierKind.STATIC, ModifierKind.FINAL));

    private static final Set<ModifierKind> PRIVATE_STATIC_FINAL =
            new HashSet<>(
                    Arrays.asList(ModifierKind.PRIVATE, ModifierKind.STATIC, ModifierKind.FINAL));

    private static final String PROCESSORS_CLASS_QUALNAME = "sorald.Processors";
    private static final String SORALD_ABSTRACT_PROCESSOR_QUALNAME =
            "sorald.processor.SoraldAbstractProcessor";

    private final Factory factory;

    public ProcessorsClassGenerator() {
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setNoClasspath(true);
        factory = launcher.getFactory();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        } else if (annotations.size() > 1) {
            processingEnv
                    .getMessager()
                    .printMessage(
                            Diagnostic.Kind.ERROR,
                            "Unexpected amount of annotations " + annotations);
            return false;
        }

        TypeElement processorAnnotation = getAnnotationFrom(ProcessorAnnotation.class, annotations);
        Set<? extends Element> processors = roundEnv.getElementsAnnotatedWith(processorAnnotation);
        try {
            CtType<?> processorsClass = createProcessorsClass(processors);
            writeType(processorsClass);
        } catch (Exception e) {
            processingEnv
                    .getMessager()
                    .printMessage(
                            Diagnostic.Kind.ERROR,
                            "Something went wrong generating the "
                                    + PROCESSORS_CLASS_QUALNAME
                                    + " class");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private CtType<?> createProcessorsClass(Set<? extends Element> processors) {
        CtType<?> processorsClass = factory.createClass(PROCESSORS_CLASS_QUALNAME);
        processorsClass.setModifiers(new HashSet<>(Arrays.asList(ModifierKind.PUBLIC)));
        CtField<String> ruleKeyToProcessor =
                addRuleKeyToProcessorField(processorsClass, processors);
        addGetProcessorMethod(processorsClass, ruleKeyToProcessor);
        addRuleDescriptionsField(processorsClass, processors);
        return processorsClass;
    }

    private void writeType(CtType<?> type) throws IOException {
        JavaFileObject processorsFile =
                processingEnv.getFiler().createSourceFile(type.getQualifiedName());

        try (PrintWriter out = new PrintWriter(processorsFile.openWriter())) {
            out.println(type.toStringWithImports());
        }
    }

    private void addRuleDescriptionsField(CtType<?> type, Set<? extends Element> elements) {
        String ruleDescriptions = generateRuleDescriptions(elements);
        factory.createField(
                type,
                PUBLIC_STATIC_FINAL,
                factory.Type().STRING,
                "RULE_DESCRIPTIONS",
                factory.createLiteral(ruleDescriptions));
    }

    private void addGetProcessorMethod(CtType<?> type, CtField<String> ruleKeyToProcessor) {
        CtTypeReference<?> returnType =
                createClassTypeRefWithUpperBound(
                        factory.createReference(SORALD_ABSTRACT_PROCESSOR_QUALNAME));
        CtMethod<String> getProcessor =
                factory.createMethod(
                        type,
                        PUBLIC_STATIC_FINAL,
                        returnType,
                        "getProcessor",
                        Collections.emptyList(),
                        Collections.emptySet());
        factory.createParameter(getProcessor, factory.Type().INTEGER_PRIMITIVE, "key");

        CtReturn<String> retStatement = factory.createReturn();
        retStatement.setReturnedExpression(
                factory.createCodeSnippetExpression(
                        ruleKeyToProcessor.getSimpleName() + ".get(key)"));
        getProcessor.setBody(factory.createCtBlock(retStatement));
    }

    private CtField<String> addRuleKeyToProcessorField(
            CtType<?> type, Set<? extends Element> elements) {
        CtTypeReference<?> mapTypeRef = factory.createCtTypeReference(Map.class);
        mapTypeRef.addActualTypeArgument(factory.Type().INTEGER);
        mapTypeRef.addActualTypeArgument(
                createClassTypeRefWithUpperBound(
                        factory.createReference(SORALD_ABSTRACT_PROCESSOR_QUALNAME)));
        return factory.createField(
                type,
                PRIVATE_STATIC_FINAL,
                mapTypeRef,
                "RULE_KEY_TO_PROCESSOR",
                generateRuleKeyToProcessorInitializer(elements));
    }

    /** Generate the CLI descriptions of rules based on ProcessorAnotations. */
    private String generateRuleDescriptions(Set<? extends Element> elements) {
        return elements.stream()
                .map(this::generateRuleDescription)
                .collect(Collectors.joining("\n"));
    }

    private String generateRuleDescription(Element processor) {
        ProcessorAnnotation processorAnnotation =
                processor.getAnnotation(ProcessorAnnotation.class);
        IncompleteProcessor incompleteAnnotation =
                processor.getAnnotation(IncompleteProcessor.class);
        return processorAnnotation.key()
                + ": "
                + processorAnnotation.description()
                + (incompleteAnnotation == null
                        ? ""
                        : "\n\t(incomplete: " + incompleteAnnotation.description() + ")");
    }

    /**
     * Generate a static initializer for a Map<Integer, ? extends SoraldAbstractProcessor> that maps
     * a rule key to its corresponding processor.
     */
    private CtExpression<?> generateRuleKeyToProcessorInitializer(Set<? extends Element> elements) {
        String mapInitializer =
                "new java.util.HashMap() {{\n"
                        + elements.stream()
                                .map(
                                        type ->
                                                "put("
                                                        + type.getAnnotation(
                                                                        ProcessorAnnotation.class)
                                                                .key()
                                                        + ","
                                                        + type.toString()
                                                        + ".class);")
                                .collect(Collectors.joining("\n"))
                        + "\n}}\n";
        return factory.createCodeSnippetExpression(mapInitializer);
    }

    /** Create a type reference to Class<? extends upperBound> */
    private CtTypeReference<?> createClassTypeRefWithUpperBound(CtTypeReference<?> upperBound) {
        CtTypeReference<?> clsWithBound = factory.Type().get(Class.class).getReference();
        CtWildcardReference wildcard = factory.createWildcardReference();
        wildcard.setBoundingType(upperBound);
        wildcard.setUpper(true);
        clsWithBound.addActualTypeArgument(wildcard);
        return clsWithBound;
    }

    /** Get a specific annotation from a set of annotations */
    private static TypeElement getAnnotationFrom(
            Class<?> annotation, Set<? extends TypeElement> annotations) {
        return annotations.stream()
                .filter(te -> te.getQualifiedName().toString().equals(annotation.getName()))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
