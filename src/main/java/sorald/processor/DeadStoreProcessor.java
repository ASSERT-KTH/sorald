package sorald.processor;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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
            List<CtStatementList> varAccessStatementLists =
                    varAccesses.stream()
                            .map(access -> access.getParent(CtStatementList.class))
                            .distinct() // TODO optimize, this will use very slow equality
                            // comparison
                            .collect(Collectors.toList());
            Map<CtElement, Integer> statementListDepths =
                    computeDepths(
                            statementList,
                            statementList.getElements(e -> e instanceof CtStatementList));

            CtStatementList commonParent =
                    findCommonParentList(varAccessStatementLists, statementListDepths);
            Optional<CtVariableAccess<?>> firstVarAccessInCommonParent =
                    varAccesses.stream()
                            .filter(
                                    access ->
                                            access.getParent(CtStatementList.class) == commonParent)
                            .findFirst();
            int indexOfFirstAccess = -1;
            for (int i = 0; i < commonParent.getStatements().size(); i++) {
                var statement = commonParent.getStatement(i);
                if (!statement.getElements(accessFilter(localVar)).isEmpty()) {
                    indexOfFirstAccess = i;
                    break;
                }
            }

            if (firstVarAccessInCommonParent.isPresent()
                    && firstVarAccessInCommonParent.get() instanceof CtVariableWrite
                    && commonParent
                                    .getStatements()
                                    .indexOf(
                                            firstVarAccessInCommonParent
                                                    .get()
                                                    .getParent(CtStatement.class))
                            == indexOfFirstAccess) {
                CtVariableWrite<?> write = (CtVariableWrite<?>) firstVarAccessInCommonParent.get();
                moveDeclarationToWrite(write, localVar);
            } else {
                commonParent.addStatement(localVar.clone());
            }

            localVar.delete();
        }
    }

    private void moveDeclarationToWrite(CtVariableWrite<?> write, CtLocalVariable<?> localVar) {
        CtAssignment assignment = write.getParent(CtAssignment.class);
        localVar.setAssignment(assignment.getAssignment());
        assignment.replace(localVar.clone());
    }

    private CtStatementList findCommonParentList(
            List<CtStatementList> statementLists, Map<CtElement, Integer> depths) {
        if (statementLists.size() == 1) {
            return statementLists.get(0);
        }

        return statementLists.stream()
                .reduce((lhs, rhs) -> findCommonParentList(lhs, rhs, depths))
                .get();
    }

    private CtStatementList findCommonParentList(
            CtStatementList lhs, CtStatementList rhs, Map<CtElement, Integer> depths) {
        while (true) {
            if (lhs == rhs) {
                return lhs;
            } else if (depths.get(lhs).equals(depths.get(rhs))) {
                lhs = lhs.getParent(CtStatementList.class);
                rhs = rhs.getParent(CtStatementList.class);
            }
            while (depths.get(lhs) > depths.get(rhs)) {
                lhs = lhs.getParent(CtStatementList.class);
            }
            while (depths.get(rhs) > depths.get(lhs)) {
                rhs = rhs.getParent(CtStatementList.class);
            }
        }
    }

    private Map<CtElement, Integer> computeDepths(
            CtElement parent, List<? extends CtElement> children) {
        Map<CtElement, Integer> idMap = new IdentityHashMap<>();
        children.forEach(child -> idMap.putIfAbsent(child, depth(child, parent)));
        return idMap;
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
