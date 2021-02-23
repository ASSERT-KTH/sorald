package sorald.processor;

import java.util.List;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.Filter;

@ProcessorAnnotation(key = 1854, description = "Unused assignments should be removed")
public class DeadStoreProcessor extends SoraldAbstractProcessor<CtStatement> {

    @Override
    protected boolean canRepairInternal(CtStatement element) {
        if (element instanceof CtLocalVariable || element instanceof CtAssignment) {
            return true;
        }
        return false;
    }

    @Override
    protected void repairInternal(CtStatement element) {
        if (element instanceof CtLocalVariable) {
            repairDeadStoreInLocalVariable((CtLocalVariable<?>) element);
        } else {
            element.delete();
        }
    }

    private void repairDeadStoreInLocalVariable(CtLocalVariable<?> localVar) {
        CtStatementList statementList = localVar.getParent(CtStatementList.class);
        List<CtVariableAccess<?>> varAccesses = statementList.getElements(accessFilter(localVar));

        if (varAccesses.isEmpty()) {
            localVar.delete();
        } else {
            varAccesses.stream()
                    .filter(access -> access instanceof CtVariableWrite)
                    .map(write -> (CtAssignment) write.getParent())
                    .findFirst()
                    .ifPresent(
                            assignment -> {
                                localVar.setAssignment(assignment.getAssignment());
                                localVar.delete();
                                assignment.replace(localVar.clone());
                            });
        }
    }

    private int depth(CtElement child, CtElement parent) {
        int depth = 0;
        for (CtElement cur = child; cur != parent; cur = cur.getParent()) {
            depth++;
        }
        return depth;
    }

    private static Filter<CtVariableAccess<?>> accessFilter(CtLocalVariable<?> localVar) {
        return (varRead) -> {
            CtVariableReference<?> ref = varRead.getVariable();
            return ref != null && ref.getDeclaration() == localVar;
        };
    }
}
