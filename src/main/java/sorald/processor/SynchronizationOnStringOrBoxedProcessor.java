package sorald.processor;

import java.util.HashMap;
import java.util.Map;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtSynchronized;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.filter.TypeFilter;

// @spotless:off
/**
 * Objects which are pooled, such as Strings or boxed primitives, and potentially reused should not be used for synchronization, since they can cause deadlocks. The transformation will do the following. If the lock is a field of the current class where the synchronization block is in, then it will simply add a new field as an `Object` lock. If the lock is obtained from another object through the `get` method, it will add a new field for the new `Object` lock and a new method to get the object.
 *
 * Example:
 * ```diff
 *    private final Boolean bLock = Boolean.FALSE;
 * +  private final Object bLockLegal = new Object();
 *    private final InnerClass i = new InnerClass();
 * -  void method1() {
 * -    synchronized(bLock) {}
 * -    synchronized(i.getLock()){}
 * -  }
 * +  void method1() {
 * +    synchronized(bLockLegal) {}
 * +    synchronized(i.getLockLegal()){}
 * +  }
 *    class InnerClass {
 *         public Boolean innerLock = Boolean.FALSE;
 * +       public Object innerLockLegal = new Object();
 *
 *         public Boolean getLock() {
 *             return this.innerLock;
 *         }
 * +       public Object getLockLegal() {
 * +           return this.innerLockLegal;
 * +       }
 *   }
 * ```
 */
// @spotless:on
@ProcessorAnnotation(
        key = "S1860",
        description = "Synchronization should not be based on Strings or boxed primitives")
public class SynchronizationOnStringOrBoxedProcessor
        extends SoraldAbstractProcessor<CtSynchronized> {
    private Map<Integer, CtVariableReference> old2NewFields;
    private Map<Integer, CtExecutableReference> old2NewMethods;

    public SynchronizationOnStringOrBoxedProcessor() {
        super();
        this.old2NewFields = new HashMap<>();
        this.old2NewMethods = new HashMap<>();
    }

    @Override
    protected void repairInternal(CtSynchronized element) {
        CtExpression<?> expression = element.getExpression();
        Factory factory = element.getFactory();
        CtFieldRead<?> fieldRead4Update;
        if (expression instanceof CtFieldRead) {
            fieldRead4Update = (CtFieldRead) expression;
        } else {
            CtExecutable<?> method = ((CtInvocation) expression).getExecutable().getDeclaration();
            CtExpression<?> oldReturnExpression =
                    ((CtReturn) method.getElements(new TypeFilter(CtReturn.class)).get(0))
                            .getReturnedExpression();
            CtFieldRead<?> oldFieldRead = (CtFieldRead) oldReturnExpression;
            CtType<?> c = (CtType) oldFieldRead.getParent(CtType.class);
            CtMethod<?> newMethod = (CtMethod) method.clone();

            if (!old2NewMethods.containsKey(method.hashCode())) {
                newMethod.setSimpleName(method.getSimpleName() + "Legal");
                newMethod.setType((((CtType) factory.Class().get(Object.class)).getReference()));
                c.addMethod(newMethod);
                ((CtInvocation) expression)
                        .setExecutable(((CtExecutable) newMethod).getReference());
            } else {
                ((CtInvocation) expression).setExecutable(old2NewMethods.get(method.hashCode()));
            }

            CtExpression<?> returnExpression =
                    ((CtReturn) newMethod.getElements(new TypeFilter(CtReturn.class)).get(0))
                            .getReturnedExpression();
            fieldRead4Update = (CtFieldRead) returnExpression;
        }
        this.updateFieldRead(fieldRead4Update);
    }

    private void updateFieldRead(CtFieldRead<?> fieldRead) {
        if (!this.old2NewFields.containsKey(fieldRead.getVariable().hashCode())) {
            CtField<?> field = fieldRead.getVariable().getDeclaration();
            CtType<?> c = (CtType) field.getParent(CtType.class);

            Factory factory = fieldRead.getFactory();

            ModifierKind[] modArr = new ModifierKind[field.getModifiers().size()];
            CtField<?> newField =
                    factory.Code()
                            .createCtField(
                                    field.getSimpleName() + "Legal",
                                    factory.Class().get(Object.class).getReference(),
                                    "new Object()",
                                    field.getModifiers().toArray(modArr));

            c.addFieldAtTop(newField);
            old2NewFields.put(
                    fieldRead.getVariable().hashCode(), ((CtVariable) newField).getReference());
            fieldRead.setVariable(((CtVariable) newField).getReference());
        } else {
            fieldRead.setVariable(old2NewFields.get(fieldRead.getVariable().hashCode()));
        }
    }
}
