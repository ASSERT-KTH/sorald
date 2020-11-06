package sorald.processor;

import javax.xml.XMLConstants;
import sorald.ProcessorAnnotation;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtCompilationUnit;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtImport;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
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
                && candidate.getParent() instanceof CtLocalVariable;
    }

    @Override
    public void process(CtInvocation<?> element) {
        super.process(element);

        if (element.getParent() instanceof CtLocalVariable) {
            CtLocalVariable<?> localVar = (CtLocalVariable<?>) element.getParent();
            processLocalVariableDocumentBuilderFactory(localVar);
        } else {
            throw new IllegalArgumentException("Unexpected element " + element);
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
        assert localVar.getType().getSimpleName().equals(DOCUMENT_BUILDER_FACTORY);
        CtBlock<?> block = localVar.getParent(CtBlock.class);
        setSafeBuilderFactoryAttributes(localVar, block);
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
