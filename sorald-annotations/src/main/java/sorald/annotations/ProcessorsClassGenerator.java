package sorald.annotations;

import spoon.Launcher;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtWildcardReference;

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
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes("sorald.annotations.ProcessorAnnotation")
public class ProcessorsClassGenerator extends AbstractProcessor {
    private static final Set<ModifierKind> PUBLIC_STATIC_FINAL =
            new HashSet<>(
                    Arrays.asList(ModifierKind.PUBLIC, ModifierKind.STATIC, ModifierKind.FINAL));

    private static final Set<ModifierKind> PRIVATE_STATIC_FINAL =
            new HashSet<>(
                    Arrays.asList(ModifierKind.PRIVATE, ModifierKind.STATIC, ModifierKind.FINAL));

    private static final String PROCESSORS_QUALNAME = "sorald.Processors";

    private final Launcher launcher;
    private final Factory factory;

    public ProcessorsClassGenerator() {
        launcher = new Launcher();
        launcher.getEnvironment().setNoClasspath(true);
        factory = launcher.getFactory();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements =
                    roundEnv.getElementsAnnotatedWith(annotation);
            CtType<?> processorsClass = createProcessorsClass(annotatedElements);
            try {
                writeProcessorsClass(processorsClass);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private void writeProcessorsClass(CtType<?> processorsClass) throws IOException {
        JavaFileObject processorsFile = processingEnv.getFiler().createSourceFile(PROCESSORS_QUALNAME);

        try (PrintWriter out = new PrintWriter(processorsFile.openWriter())) {
            out.println(processorsClass.toStringWithImports());
        }
    }

    private CtType<?> createProcessorsClass(Set<? extends Element> types) {
        CtType<?> processorsClass = factory.createClass(PROCESSORS_QUALNAME);
        addGetProcessor(processorsClass);
        addGetRuleDescriptions(processorsClass, types);
        addRuleKeyToProcessorField(processorsClass, types);
        return processorsClass;
    }

    private void addGetRuleDescriptions(CtType<?> type, Set<? extends Element> types) {
        CtMethod<String> getRuleDescriptions =
                factory.createMethod(
                        type,
                        PUBLIC_STATIC_FINAL,
                        factory.Type().STRING,
                        "getRuleDescriptions",
                        Collections.emptyList(),
                        Collections.emptySet());
        CtReturn<String> retStatement = factory.createReturn();
        retStatement.setReturnedExpression(factory.createLiteral(generateRuleDescriptions(types)));
        getRuleDescriptions.setBody(factory.createCtBlock(retStatement));
    }

    private void addGetProcessor(CtType<?> type) {
        Set<ModifierKind> publicStaticFinal =
                new HashSet<>(
                        Arrays.asList(
                                ModifierKind.PUBLIC, ModifierKind.STATIC, ModifierKind.FINAL));

        CtTypeReference<?> cls = factory.createCtTypeReference(Class.class);
        CtMethod<String> getProcessor =
                factory.createMethod(
                        type,
                        publicStaticFinal,
                        cls,
                        "getProcessor",
                        Collections.emptyList(),
                        Collections.emptySet());
        factory.createParameter(getProcessor, factory.Type().INTEGER_PRIMITIVE, "key");

        CtReturn<String> retStatement = factory.createReturn();
        retStatement.setReturnedExpression(
                factory.createCodeSnippetExpression("RULE_KEY_TO_PROCESSOR.get(key)"));
        getProcessor.setBody(factory.createCtBlock(retStatement));
    }

    private void addRuleKeyToProcessorField(
            CtType<?> processorsClass, Set<? extends Element> types) {
        CtTypeReference<?> mapTypeRef = factory.createCtTypeReference(Map.class);
        mapTypeRef.addActualTypeArgument(factory.Type().INTEGER);
        mapTypeRef.addActualTypeArgument(
                createClassTypeRefWithUpperBound(
                        factory.createReference("sorald.processor.SoraldAbstractProcessor")));
        factory.createField(
                processorsClass,
                PRIVATE_STATIC_FINAL,
                mapTypeRef,
                "RULE_KEY_TO_PROCESSOR",
                generateRuleKeyToProcessorInitializer(types));
    }

    private CtTypeReference<?> createClassTypeRefWithUpperBound(CtTypeReference<?> upperBound) {
        CtTypeReference<?> clsWithBound = factory.Type().get(Class.class).getReference();
        CtWildcardReference wildcard = factory.createWildcardReference();
        wildcard.setBoundingType(upperBound);
        wildcard.setUpper(true);
        clsWithBound.addActualTypeArgument(wildcard);
        return clsWithBound;
    }

    private String generateRuleDescriptions(Set<? extends Element> types) {
        return types.stream()
                .map(type -> type.getAnnotation(ProcessorAnnotation.class))
                .map(annotation -> annotation.key() + ": " + annotation.description())
                .collect(Collectors.joining("\n"));
    }

    private CtExpression<?> generateRuleKeyToProcessorInitializer(Set<? extends Element> types) {
        String mapInitializer =
                "new java.util.HashMap() {{\n"
                        + types.stream()
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
}
