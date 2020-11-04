package sorald.processor;

import javax.xml.XMLConstants;
import sorald.ProcessorAnnotation;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.code.CtVariableAccess;
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
    @Override
    public boolean isToBeProcessed(CtInvocation<?> candidate) {
        return super.isToBeProcessedAccordingToStandards(candidate)
                && candidate.getType().getSimpleName().equals("DocumentBuilderFactory")
                && candidate.getParent() instanceof CtLocalVariable<?>;
    }

    @Override
    public void process(CtInvocation<?> element) {
        CtLocalVariable<?> builderFactoryVar = (CtLocalVariable<?>) element.getParent();
        CtBlock<?> block = (CtBlock<?>) builderFactoryVar.getParent();

        CtFieldRead<String> accessExternalDtd = readXmlConstant("ACCESS_EXTERNAL_DTD");
        CtFieldRead<String> accessExternalSchema = readXmlConstant("ACCESS_EXTERNAL_SCHEMA");

        CtLiteral<Object> emptyString = getFactory().createLiteral("");
        CtInvocation<?> setExternalDtd =
                createSetAttributeInvocation(builderFactoryVar, accessExternalDtd, emptyString);
        CtInvocation<?> setExternalSchema =
                createSetAttributeInvocation(builderFactoryVar, accessExternalSchema, emptyString);

        int statementIdx = block.getStatements().indexOf(builderFactoryVar);
        block.addStatement(statementIdx + 1, setExternalSchema);
        block.addStatement(statementIdx + 1, setExternalDtd);

        ensureTypeImported(element, getFactory().Type().get(XMLConstants.class));

        super.process(element);
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

    /** @return An invocation localVar.setAttribute(key, value). */
    private <T> CtInvocation<T> createSetAttributeInvocation(
            CtLocalVariable<T> localVar, CtExpression<String> key, CtExpression<Object> value) {
        CtType<T> builderFactory = localVar.getType().getTypeDeclaration();
        CtMethod<T> setAttribute =
                builderFactory.getMethod(
                        "setAttribute", getFactory().Type().STRING, getFactory().Type().OBJECT);
        return getFactory()
                .createInvocation(read(localVar), setAttribute.getReference(), key, value);
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
