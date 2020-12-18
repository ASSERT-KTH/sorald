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

@ProcessorAnnotation(
        key = 1860,
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
    protected boolean canRepairInternal(CtSynchronized element) {
        CtExpression<?> expression = element.getExpression();
        if (!expression.getType().toString().equals("String")
                && !expression.getType().unbox().isPrimitive()) {
            return false;
        }
        CtFieldRead<?> fieldRead;
        if (expression instanceof CtFieldRead) {
            fieldRead = (CtFieldRead) expression;
        } else if (expression instanceof CtInvocation) {
            CtExecutable<?> method = ((CtInvocation) expression).getExecutable().getDeclaration();
            CtExpression<?> returnExpression =
                    ((CtReturn) method.getElements(new TypeFilter(CtReturn.class)).get(0))
                            .getReturnedExpression();
            if (returnExpression instanceof CtFieldRead) {
                fieldRead = (CtFieldRead) returnExpression;
            } else {
                /* don't support multiple recursive call */
                return false;
            }
        } else {
            return false;
        }

        CtField<?> field = fieldRead.getVariable().getDeclaration();
        if (field != null) {
            return true;
        }

        return false;
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
