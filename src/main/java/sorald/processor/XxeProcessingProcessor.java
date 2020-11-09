package sorald.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import sorald.ProcessorAnnotation;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtCompilationUnit;
import spoon.reflect.declaration.CtImport;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

@ProcessorAnnotation(
        key = 2755,
        description = "XML parsers should not be vulnerable to XXE attacks")
public class XxeProcessingProcessor extends SoraldAbstractProcessor<CtInvocation<?>> {
    private static final String ACCESS_EXTERNAL_DTD = "ACCESS_EXTERNAL_DTD";
    private static final String ACCESS_EXTERNAL_SCHEMA = "ACCESS_EXTERNAL_SCHEMA";
    private static final String DOCUMENT_BUILDER_FACTORY = "DocumentBuilderFactory";

    @Override
    public boolean isToBeProcessed(CtInvocation<?> candidate) {
        return super.isToBeProcessedAccordingToStandards(candidate)
                && candidate
                        .getExecutable()
                        .getType()
                        .getSimpleName()
                        .equals(DOCUMENT_BUILDER_FACTORY);
    }

    @Override
    public void process(CtInvocation<?> element) {
        super.process(element);
        CtType<?> declaringType = element.getParent(CtType.class);

        CtMethod<?> docBuilderFactoryMethod =
                createDocumentBuilderFactoryMethod(element, declaringType);
        ensureTypeImported(declaringType, getFactory().Type().get(XMLConstants.class));
        ensureTypeImported(declaringType, element.getType().getTypeDeclaration());

        CtInvocation<?> safeCreateDocBuilderFactory = invoke(docBuilderFactoryMethod);
        element.replace(safeCreateDocBuilderFactory);
    }

    /**
     * Create the following method on the provided type, where FACTORYTYPE is the type of the target
     * of the newInstanceInvocation (e.g. {@link javax.xml.parsers.DocumentBuilderFactory}). <br>
     * <code>
     *     private static FACTORYTYPE createFACTORYTYPE() {
     *         FACTORYTYPE factory = newInstanceInvocation();
     *         factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD);
     *         factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA);
     *         return factory;
     *     }
     * </code>
     *
     * @param newInstanceInvocation An invocation to a factory class' "newInstance" method, such as
     *     {@link DocumentBuilderFactory#newInstance()}
     * @param declaringType The type in which the invocation exists.
     * @return A method to create a safe factory, declared on the given type
     */
    private CtMethod<?> createDocumentBuilderFactoryMethod(
            CtInvocation<?> newInstanceInvocation, CtType<?> declaringType) {
        CtLocalVariable<?> builderFactoryVariable =
                createLocalVariable("factory", newInstanceInvocation);

        List<CtStatement> statements = new ArrayList<>();
        statements.add(builderFactoryVariable);
        statements.addAll(setSafeBuilderFactoryAttributes(builderFactoryVariable));

        return createPrivateStaticMethod(
                "create" + newInstanceInvocation.getType().getSimpleName(),
                declaringType,
                statements,
                read(builderFactoryVariable));
    }

    /**
     * Return the following statements: <br>
     * <code>
     *     localVar.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD);
     *     localVar.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA);
     * </code>
     */
    private List<? extends CtInvocation<?>> setSafeBuilderFactoryAttributes(
            CtLocalVariable<?> localVar) {
        CtFieldRead<String> accessExternalDtd = readXmlConstant(ACCESS_EXTERNAL_DTD);
        CtFieldRead<String> accessExternalSchema = readXmlConstant(ACCESS_EXTERNAL_SCHEMA);
        CtLiteral<Object> emptyString = getFactory().createLiteral("");
        CtInvocation<?> setExternalDtd =
                createSetAttributeInvocation(read(localVar), accessExternalDtd, emptyString);
        CtInvocation<?> setExternalSchema =
                createSetAttributeInvocation(read(localVar), accessExternalSchema, emptyString);
        return Arrays.asList(setExternalDtd, setExternalSchema);
    }

    /**
     * @return A private static method with the given name, receiver type, return expression and
     *     statements in the body.
     */
    private <T> CtMethod<T> createPrivateStaticMethod(
            String name,
            CtType<?> receiver,
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
        returnType.getPackage().setImplicit(true);
        CtMethod<T> method =
                getFactory()
                        .createMethod(
                                receiver,
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
     * Ensure that the given type is imported in the compilation unit in which element is defined.
     */
    private void ensureTypeImported(CtType<?> element, CtType<?> mustBeImported) {
        CtType<?> declaringType =
                element.isTopLevel() ? element : element.getParent(CtType::isTopLevel);
        CtCompilationUnit cu = getFactory().CompilationUnit().getOrCreate(declaringType);
        CtImport requiredImport = getFactory().createImport(mustBeImported.getReference());

        for (CtImport imp : cu.getImports()) {
            if (imp.toString().equals(requiredImport.toString())) {
                return;
            }
        }

        cu.getImports().add(requiredImport);
    }

    /**
     * @param constantName The name of an XMLConstants static variable
     * @return A field read of XMLConstants.[constantName]
     */
    private CtFieldRead<String> readXmlConstant(String constantName) {
        CtType<XMLConstants> xmlConstants = getFactory().Type().get(XMLConstants.class);
        CtTypeAccess<?> xmlConstantsAccess =
                getFactory().createTypeAccess(getReferenceWithImplicitPackage(xmlConstants));
        CtFieldRead<String> fieldRead = getFactory().createFieldRead();
        fieldRead.setTarget(xmlConstantsAccess);
        CtFieldReference fieldRef = xmlConstants.getDeclaredField(constantName);
        fieldRead.setVariable(fieldRef);
        return fieldRead;
    }

    /** @return An invocation receiver.setAttribute(key, value). */
    private <T> CtInvocation<T> createSetAttributeInvocation(
            CtExpression<T> receiver, CtExpression<String> key, CtExpression<Object> value) {
        CtType<T> builderFactory = receiver.getType().getTypeDeclaration();
        CtMethod<T> setAttribute =
                builderFactory.getMethod(
                        "setAttribute", getFactory().Type().STRING, getFactory().Type().OBJECT);
        return getFactory().createInvocation(receiver, setAttribute.getReference(), key, value);
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

    private <T> CtTypeReference<T> getReferenceWithImplicitPackage(CtType<T> type) {
        CtTypeReference<T> ref = type.getReference();
        ref.getPackage().setImplicit(true);
        return ref;
    }
}
