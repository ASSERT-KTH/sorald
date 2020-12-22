package sorald.processor;

import java.util.HashMap;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtImport;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.ImportScannerImpl;

@ProcessorAnnotation(key = 3032, description = "JEE applications should not \"getClassLoader\"")
public class GetClassLoaderProcessor extends SoraldAbstractProcessor<CtInvocation<?>> {
    private HashMap<Integer, Boolean> hashCodesOfTypesUsingJEE = new HashMap<Integer, Boolean>();

    @Override
    protected boolean canRepairInternal(CtInvocation<?> invocation) {
        String invocationStr = invocation.toString();
        if (invocationStr.contains("getClass().getClassLoader()")
                || invocationStr.contains(".class.getClassLoader()")) {
            CtType t = (CtType) invocation.getParent(CtType.class);
            CtType topParent = t.getReference().getTopLevelType().getDeclaration();
            Integer hashCode = new Integer(topParent.hashCode());
            if (this.hashCodesOfTypesUsingJEE.get(hashCode) == null) {
                ImportScannerImpl scanner = new ImportScannerImpl();
                scanner.scan(topParent);
                this.hashCodesOfTypesUsingJEE.put(hashCode, new Boolean(false));
                for (CtImport imp : scanner.getAllImports()) {
                    if (imp.toString().contains("javax")) {
                        this.hashCodesOfTypesUsingJEE.put(hashCode, new Boolean(true));
                    }
                }
            }
            Boolean useJEE = this.hashCodesOfTypesUsingJEE.get(hashCode);
            return useJEE.booleanValue();
        }
        return false;
    }

    @Override
    protected void repairInternal(CtInvocation<?> element) {
        Factory factory = element.getFactory();
        CtClass<?> c = factory.Class().get(Thread.class);
        CtTypeAccess<?> access = factory.createTypeAccess(c.getReference());
        CtMethod<?> method1 = c.getMethodsByName("currentThread").get(0);
        CtMethod<?> method2 = c.getMethodsByName("getContextClassLoader").get(0);
        CtInvocation invo1 = factory.createInvocation(access, method1.getReference());
        CtInvocation invo2 = factory.createInvocation(invo1, method2.getReference());
        element.replace(invo2);
    }
}
