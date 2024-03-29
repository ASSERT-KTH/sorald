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
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.filter.TypeFilter;

@ProcessorAnnotation(
        key = "S1860",
        description = "Synchronization should not be based on Strings or boxed primitives")
public class SynchronizationOnStringOrBoxedProcessor
        extends SoraldAbstractProcessor<CtSynchronized> {
    private Map<String, CtVariableReference> old2NewFields;

    public SynchronizationOnStringOrBoxedProcessor() {
        super();
        this.old2NewFields = new HashMap<>();
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

            newMethod.setSimpleName(method.getSimpleName() + "Legal");
            newMethod.setType((((CtType) factory.Class().get(Object.class)).getReference()));
            c.addMethod(newMethod);
            ((CtInvocation) expression).setExecutable(((CtExecutable) newMethod).getReference());

            CtExpression<?> returnExpression =
                    ((CtReturn) newMethod.getElements(new TypeFilter(CtReturn.class)).get(0))
                            .getReturnedExpression();
            fieldRead4Update = (CtFieldRead) returnExpression;
        }
        this.updateFieldRead(fieldRead4Update);
    }

    private void updateFieldRead(CtFieldRead<?> fieldRead) {
        if (!this.old2NewFields.containsKey(fieldRead.getVariable().getQualifiedName())) {
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
                    fieldRead.getVariable().getQualifiedName(),
                    ((CtVariable) newField).getReference());
            fieldRead.setVariable(((CtVariable) newField).getReference());
        } else {
            fieldRead.setVariable(old2NewFields.get(fieldRead.getVariable().getQualifiedName()));
        }
    }
}
