package sorald.processor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import sorald.ProcessorAnnotation;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtCompilationUnit;
import spoon.reflect.declaration.CtElement;
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
                        .equals(DOCUMENT_BUILDER_FACTORY)
                && (candidate.getParent() instanceof CtLocalVariable
                        || candidate.getParent().getParent() instanceof CtLocalVariable);
    }

    @Override
    public void process(CtInvocation<?> element) {
        super.process(element);

        if (element.getParent() instanceof CtLocalVariable) {
            CtLocalVariable<?> localVar = (CtLocalVariable<?>) element.getParent();
            assert localVar.getType().getSimpleName().equals(DOCUMENT_BUILDER_FACTORY);
            processLocalVariableDocumentBuilderFactory(localVar);
        } else if (element.getParent() instanceof CtInvocation
                && element.getParent().getParent() instanceof CtLocalVariable) {
            processChainedBuilderFactory(element);
        }
    }

    /**
     * Processing only for the case where the factory is declared separately from the document
     * builder. Example: <br>
     * <code>
     *     // Input
     *     DocumentBuilderFactory df = DocumentBuilderFactory.newInstance(); // Noncompliant
     *     DocumentBuilder = df.newDocumentBuilder();
     *
     *     // Transform
     *     DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
     *     df.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
     *     df.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
     *     DocumentBuilder = df.newDocumentBuilder();
     * </code>
     *
     * @param localVar The variable declaration "DocumentBuilderFactory df;"
     */
    private void processLocalVariableDocumentBuilderFactory(CtLocalVariable<?> localVar) {
        CtBlock<?> block = localVar.getParent(CtBlock.class);
        setSafeBuilderFactoryAttributes(localVar, block);
    }

    /**
     * Processing only for the case where the creation of the builder is chained with the creation
     * of the factory, i.e. something like so: <code>
     * DocumentBuilderFactory.newInstance().createDocumentBuilder()</code> All of that is replaced
     * with a method invocation to <code>createDocumentBuilder</code> defined like so: <br>
     * <code>
     * private static javax.xml.parsers.DocumentBuilder createDocumentBuilder() {
     *     DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
     *     df.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
     *     df.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
     *     return df.newDocumentBuilder();
     * }
     * </code>
     *
     * @param newInstanceInvocation An invocation to {@link DocumentBuilderFactory#newInstance()}
     */
    private <T> void processChainedBuilderFactory(CtInvocation<T> newInstanceInvocation) {
        assert newInstanceInvocation.getParent() instanceof CtInvocation;
        CtInvocation<?> newBuilderInvocation = (CtInvocation<?>) newInstanceInvocation.getParent();
        CtType<?> type = newInstanceInvocation.getParent(CtType.class);

        CtLocalVariable<T> builderFactoryVariable =
                createLocalVariable("df", newInstanceInvocation);
        CtVariableAccess<T> varRead =
                getFactory().createVariableRead(builderFactoryVariable.getReference(), false);

        CtInvocation<?> newBuilderReturnExpr = newBuilderInvocation.clone();
        newBuilderReturnExpr.setTarget(varRead);

        CtMethod<?> method =
                createPrivateStaticMethod(
                        "createDocumentBuilder",
                        type,
                        newBuilderReturnExpr,
                        builderFactoryVariable);
        setSafeBuilderFactoryAttributes(builderFactoryVariable, method.getBody());

        newBuilderInvocation.replace(
                getFactory()
                        .createInvocation(
                                getFactory().createThisAccess(type.getReference(), true),
                                method.getReference()));
    }

    /**
     * Add the following two statements to block: <br>
     * <code>
     *     localVar.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD);
     *     localVar.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA);
     * </code>
     */
    private void setSafeBuilderFactoryAttributes(CtLocalVariable<?> localVar, CtBlock<?> block) {
        CtFieldRead<String> accessExternalDtd = readXmlConstant(ACCESS_EXTERNAL_DTD);
        CtFieldRead<String> accessExternalSchema = readXmlConstant(ACCESS_EXTERNAL_SCHEMA);
        CtLiteral<Object> emptyString = getFactory().createLiteral("");
        CtInvocation<?> setExternalDtd =
                createSetAttributeInvocation(read(localVar), accessExternalDtd, emptyString);
        CtInvocation<?> setExternalSchema =
                createSetAttributeInvocation(read(localVar), accessExternalSchema, emptyString);

        int statementIdx = block.getStatements().indexOf(localVar);
        block.addStatement(statementIdx + 1, setExternalSchema);
        block.addStatement(statementIdx + 1, setExternalDtd);
        ensureTypeImported(localVar, getFactory().Type().get(XMLConstants.class));
    }

    /**
     * @return A private static method with the given name, receiver type, return expression and
     *     statements in the body.
     */
    private <T> CtMethod<T> createPrivateStaticMethod(
            String name, CtType<?> receiver, CtExpression<T> returnExp, CtStatement... statements) {
        CtReturn<T> returnStatement = getFactory().createReturn();
        returnStatement.setReturnedExpression(returnExp);

        CtBlock<T> body = getFactory().createBlock();
        Arrays.stream(statements).forEach(body::addStatement);
        body.addStatement(returnStatement);

        Set<ModifierKind> modifiers =
                new HashSet<>(Arrays.asList(ModifierKind.PRIVATE, ModifierKind.STATIC));
        CtMethod<T> method =
                getFactory()
                        .createMethod(
                                receiver,
                                modifiers,
                                returnExp.getType().getTypeDeclaration().getReference(),
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
    private void ensureTypeImported(CtElement element, CtType<?> mustBeImported) {
        CtType<?> declaringType = element.getParent(CtType::isTopLevel);
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

    private <T> CtTypeReference<T> getReferenceWithImplicitPackage(CtType<T> type) {
        CtTypeReference<T> ref = type.getReference();
        ref.getPackage().setImplicit(true);
        return ref;
    }
}
