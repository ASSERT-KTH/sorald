package sorald.processor;

import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtSynchronized;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

/**
 * Any invocation using getClass will be typechecked if the object's invoked by `getClass` is final or an enum. If not, the invocation will be transformed to `.class` instead of `getClass`.
 *
 * Example:
 * ```diff
 * class SynchronizationOnGetClass {
 * -  public void method1() {
 * -    InnerClass i = new InnerClass();
 * -    synchronized (i.getObject().getClass()) { // Noncompliant - object's modifier is unknown, assume non-final nor enum
 * -  }
 * +  public void method1() {
 * +    InnerClass i = new InnerClass();
 * +    synchronized(Object.class) {}
 * +  }
 * -  public void method2() {
 * -    synchronized (getClass()) {}
 * -  }
 * +  public void method2() {
 * +    synchronized(SynchronizationOnGetClass.class) {}
 * +  }
 * }
 * ```
 */
@ProcessorAnnotation(
        key = "S3067",
        description = "\"getClass\" should not be used for synchronization")
public class SynchronizationOnGetClassProcessor extends SoraldAbstractProcessor<CtSynchronized> {

    @Override
    protected void repairInternal(CtSynchronized element) {
        CtExpression<?> expression = element.getExpression();
        CtTypeReference<?> typeRef;
        if (expression.toString().equals("getClass()")) {
            /* implicit this case */
            typeRef = ((CtType) expression.getParent(CtType.class)).getReference();
        } else {
            typeRef = ((CtInvocation) expression).getTarget().getType();
        }

        Factory factory = element.getFactory();
        CtFieldAccess<?> classAccess = factory.Code().createClassAccess(typeRef);

        expression.replace(classAccess);
    }
}
