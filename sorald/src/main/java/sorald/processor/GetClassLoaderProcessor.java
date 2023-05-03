package sorald.processor;

import sorald.annotations.ProcessorAnnotation;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

import java.util.HashMap;

@ProcessorAnnotation(key = "S3032", description = "JEE applications should not \"getClassLoader\"")
public class GetClassLoaderProcessor extends SoraldAbstractProcessor<CtInvocation<?>> {
    private HashMap<Integer, Boolean> hashCodesOfTypesUsingJEE = new HashMap<Integer, Boolean>();

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
