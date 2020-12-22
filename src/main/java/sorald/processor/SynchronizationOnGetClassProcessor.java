package sorald.processor;

import java.util.Set;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtSynchronized;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

@ProcessorAnnotation(
        key = 3067,
        description = "\"getClass\" should not be used for synchronization")
public class SynchronizationOnGetClassProcessor extends SoraldAbstractProcessor<CtSynchronized> {

    @Override
    protected boolean canRepairInternal(CtSynchronized element) {
        CtExpression<?> expression = element.getExpression();
        if (expression.toString().endsWith("getClass()")) {
            CtExpression target = ((CtInvocation) expression).getTarget();
            if (target != null) {
                CtType<?> type = target.getType().getDeclaration();
                if (type == null) {
                    /* not in class path, but still fail according to SonarQube */
                    return true;
                }
                if (this.enclosingTypeIsFinalOrEnum(type)) {
                    return false;
                } else {
                    return true;
                }
            } else {
                /* implicit this */
                CtType<?> type = ((CtType) element.getParent(CtType.class));
                if (this.enclosingTypeIsFinalOrEnum(type)) {
                    return false;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

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

    private boolean enclosingTypeIsFinalOrEnum(CtType<?> type) {
        Set<ModifierKind> modifiers = type.getModifiers();
        if (modifiers.contains(ModifierKind.valueOf("FINAL")) || type.isEnum()) {
            return true;
        } else {
            return false;
        }
    }
}
