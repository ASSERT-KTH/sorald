package sorald.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import sorald.annotations.IncompleteProcessor;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.Filter;

@IncompleteProcessor(
        description =
                "This processor is a WIP and currently supports a subset of rule 2755. "
                        + "See Sorald's documentation for details.")
@ProcessorAnnotation(
        key = 2755,
        description = "XML parsers should not be vulnerable to XXE attacks")
public class XxeProcessingProcessor extends SoraldAbstractProcessor<CtInvocation<?>> {
    private static final String ACCESS_EXTERNAL_DTD = "ACCESS_EXTERNAL_DTD";
    private static final String ACCESS_EXTERNAL_SCHEMA = "ACCESS_EXTERNAL_SCHEMA";
    private static final String ACCESS_EXTERNAL_STYLESHEET = "ACCESS_EXTERNAL_STYLESHEET";
    private static final String FEATURE_SECURE_PROCESSING = "FEATURE_SECURE_PROCESSING";

    private static final String DOCUMENT_BUILDER_FACTORY = "DocumentBuilderFactory";
    private static final String TRANSFORMER_FACTORY = "TransformerFactory";
    private static final String XML_INPUT_FACTORY = "XMLInputFactory";

    @Override
    public boolean isToBeProcessed(CtInvocation<?> candidate) {
        return super.isToBeProcessedAccordingToStandards(candidate) && isSupported(candidate);
    }

    /** Check if the target of the invocation is of a type currently supported by this processor */
    private static boolean isSupported(CtInvocation<?> candidate) {
        List<String> supportedNames =
                Arrays.asList(DOCUMENT_BUILDER_FACTORY, TRANSFORMER_FACTORY, XML_INPUT_FACTORY);
        return supportedNames.contains(candidate.getType().getSimpleName());
    }

    @Override
    public void process(CtInvocation<?> element) {
        super.process(element);
        CtType<?> declaringType = element.getParent(CtType.class);

        CtMethod<?> factoryMethod = createFactoryMethod(element, declaringType);

        CtInvocation<?> safeCreateDocBuilderFactory = invoke(factoryMethod);
        removeSetSecureProcessingCalls(element);
        element.replace(safeCreateDocBuilderFactory);
    }

    /**
     * Remove any invocation on the form <code>
     * someFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)</code>, as it is
     * unreliable.
     */
    private void removeSetSecureProcessingCalls(CtInvocation<?> element) {
        final CtLocalVariable<?> variable = element.getParent(e -> true);
        if (variable == null) {
            return;
        }

        Filter<CtElement> isInvocationOnVariable =
                e -> {
                    if (e instanceof CtInvocation
                            && ((CtInvocation<?>) e).getTarget() instanceof CtVariableAccess) {
                        CtVariableAccess<?> varAccess =
                                (CtVariableAccess<?>) ((CtInvocation<?>) e).getTarget();
                        return variable.equals(varAccess.getVariable().getDeclaration());
                    } else {
                        return false;
                    }
                };

        CtBlock<?> enclosingBlock = variable.getParent(e -> true);
        List<CtInvocation<?>> invocations =
                enclosingBlock.filterChildren(isInvocationOnVariable).list();

        List<CtExpression<?>> expectedArguments =
                List.of(
                        readXmlConstant(FEATURE_SECURE_PROCESSING),
                        getFactory().createLiteral(true));
        for (CtInvocation<?> invocation : invocations) {
            if (invocation.getExecutable().getSimpleName().equals("setFeature")
                    && expectedArguments.equals(invocation.getArguments())) {
                invocation.delete();
            }
        }
    }

    /**
     * Create the following method on the provided type, where FACTORYTYPE is the type of the target
     * of the newInstanceInvocation (e.g. {@link javax.xml.parsers.DocumentBuilderFactory}). <br>
     * <code>
     *     private static FACTORYTYPE createFACTORYTYPE() {
     *         FACTORYTYPE factory = newInstanceInvocation();
     *         // set safe attributes
     *         return factory;
     *     }
     * </code><br>
     * The "safe attributes" vary from factory to factory. For example, for the {@link
     * DocumentBuilderFactory}, it would look like so: <br>
     * <code>
     *         factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
     *         factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
     * </code>
     *
     * @param newInstanceInvocation An invocation to a factory class' "newInstance" method, such as
     *     {@link DocumentBuilderFactory#newInstance()}
     * @param declaringType The type in which the invocation exists.
     * @return A method to create a safe factory, declared on the given type
     */
    private CtMethod<?> createFactoryMethod(
            CtInvocation<?> newInstanceInvocation, CtType<?> declaringType) {
        CtLocalVariable<?> builderFactoryVariable =
                createLocalVariable("factory", newInstanceInvocation);

        List<CtStatement> statements = new ArrayList<>();
        statements.add(builderFactoryVariable);
        statements.addAll(setXMLConstantsAttributesToEmptyString(builderFactoryVariable));

        return createPrivateStaticMethod(
                "create" + newInstanceInvocation.getType().getSimpleName(),
                declaringType,
                statements,
                read(builderFactoryVariable));
    }

    /**
     * Return one of the following statements for each attribute passed in: <br>
     * <code>
     *     localVar.setAttribute(XMLConstants.ATTR, "");
     * </code>
     */
    private List<? extends CtInvocation<?>> setXMLConstantsAttributesToEmptyString(
            CtLocalVariable<?> localVar) {
        CtLiteral<Object> emptyString = getFactory().createLiteral("");
        Function<CtFieldRead<String>, ? extends CtInvocation<?>> setAttrToEmptyString =
                (xmlAttrRead) ->
                        createSetAttributeInvocation(read(localVar), xmlAttrRead, emptyString);
        return getXMLConstantNamesFor(localVar.getType()).stream()
                .map(this::readXmlConstant)
                .map(setAttrToEmptyString)
                .collect(Collectors.toList());
    }

    /**
     * @return A private static method with the given name, target type, return expression and
     *     statements in the body.
     */
    private <T> CtMethod<T> createPrivateStaticMethod(
            String name,
            CtType<?> target,
            List<? extends CtStatement> statements,
            CtExpression<T> returnExp) {
        CtReturn<T> returnStatement = getFactory().createReturn();
        returnStatement.setReturnedExpression(returnExp);

        CtBlock<T> body = getFactory().createBlock();
        statements.forEach(body::addStatement);
        body.addStatement(returnStatement);

        Set<ModifierKind> modifiers =
                new HashSet<>(Arrays.asList(ModifierKind.PRIVATE, ModifierKind.STATIC));
        CtTypeReference<?> returnType = returnExp.getType().getTypeDeclaration().getReference();
        CtMethod<T> method =
                getFactory()
                        .createMethod(
                                target,
                                modifiers,
                                returnType,
                                name,
                                Collections.emptyList(),
                                Collections.emptySet());
        method.setBody(body);
        return method;
    }

    /** @return A local variable initialized to the given expression. */
    private <T> CtLocalVariable<T> createLocalVariable(String variableName, CtExpression<T> expr) {
        return getFactory().createLocalVariable(expr.getType().clone(), variableName, expr.clone());
    }

    /**
     * @param constantName The name of an XMLConstants static variable
     * @return A field read of XMLConstants.[constantName]
     */
    private CtFieldRead<String> readXmlConstant(String constantName) {
        CtType<XMLConstants> xmlConstants = getFactory().Type().get(XMLConstants.class);
        CtTypeAccess<?> xmlConstantsAccess =
                getFactory().createTypeAccess(xmlConstants.getReference());
        CtFieldRead<String> fieldRead = getFactory().createFieldRead();
        fieldRead.setTarget(xmlConstantsAccess);
        CtFieldReference fieldRef = xmlConstants.getDeclaredField(constantName);
        fieldRead.setVariable(fieldRef);
        return fieldRead;
    }

    /**
     * @return An invocation target.SET_ATTRIBUTE(key, value), where the exact name of SET_ATTRIBUTE
     *     depends on the target type.
     */
    private <T> CtInvocation<T> createSetAttributeInvocation(
            CtExpression<T> target, CtExpression<String> key, CtExpression<Object> value) {
        CtType<T> builderFactory = target.getType().getTypeDeclaration();
        String setAttrMethodName = getAttributeSetterMethodName(target.getType());
        CtMethod<T> setAttribute =
                builderFactory.getMethod(
                        setAttrMethodName, getFactory().Type().STRING, getFactory().Type().OBJECT);
        return getFactory().createInvocation(target, setAttribute.getReference(), key, value);
    }

    private <T> CtVariableAccess<T> read(CtLocalVariable<T> localVar) {
        return getFactory().createVariableRead(localVar.getReference(), localVar.isStatic());
    }

    private CtInvocation<?> invoke(CtMethod<?> method) {
        CtType<?> declaringType = method.getParent(CtType.class);
        CtThisAccess<?> thisAccess =
                getFactory().createThisAccess(declaringType.getReference(), true);
        return getFactory().createInvocation(thisAccess, method.getReference());
    }

    private static List<String> getXMLConstantNamesFor(CtTypeReference<?> type) {
        return type.getSimpleName().equals(TRANSFORMER_FACTORY)
                ? Arrays.asList(ACCESS_EXTERNAL_DTD, ACCESS_EXTERNAL_STYLESHEET)
                : Arrays.asList(ACCESS_EXTERNAL_DTD, ACCESS_EXTERNAL_SCHEMA);
    }

    private static String getAttributeSetterMethodName(CtTypeReference<?> type) {
        switch (type.getSimpleName()) {
            case DOCUMENT_BUILDER_FACTORY:
            case TRANSFORMER_FACTORY:
                return "setAttribute";
            case XML_INPUT_FACTORY:
                return "setProperty";
            default:
                throw new IllegalArgumentException(
                        "Missing method name for " + type.getSimpleName());
        }
    }
}
