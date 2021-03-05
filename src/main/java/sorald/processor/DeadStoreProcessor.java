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
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.Filter;

@ProcessorAnnotation(key = 1854, description = "Unused assignments should be removed")
public class DeadStoreProcessor extends SoraldAbstractProcessor<CtStatement> {

    @Override
    protected void repairInternal(CtStatement element) {
        if (element instanceof CtLocalVariable) {
            retainDeclarationOnVariableUse((CtLocalVariable<?>) element);
        }
        element.delete();
    }

    /**
     * A dead store in a local variable means that the initializer is a dead store, but the variable
     * may still be used elsewhere. Thus, we must ensure that we don't end up with a
     * declaration-less variable on the loose.
     *
     * @param localVar The local variable declaration that is to be deleted due to a dead
     *     initializer.
     */
    private void retainDeclarationOnVariableUse(CtLocalVariable<?> localVar) {
        CtStatementList statementList = localVar.getParent(CtStatementList.class);
        List<CtVariableAccess<?>> varAccesses = statementList.getElements(accessFilter(localVar));

        if (!varAccesses.isEmpty()) {
            createNewDeclaration(statementList, varAccesses, localVar);
        }
    }

    /**
     * Given that there are variable accesses other than the initial dead store, we must create a
     * new declaration. This method does so with the tightest possible scope, merging the
     * declaration with a variable write if possible.
     *
     * @param statementList The statement list in which the variable declaration appears.
     * @param varAccesses All accesses to the variable.
     * @param localVar The variable declaration itself.
     */
    private void createNewDeclaration(
            CtStatementList statementList,
            List<CtVariableAccess<?>> varAccesses,
            CtLocalVariable<?> localVar) {
        List<CtStatementList> statementListsWithVarAccess =
                varAccesses.stream()
                        .map(access -> access.getParent(CtStatementList.class))
                        .distinct() // TODO optimize, this uses taxing equality comparison
                        .collect(Collectors.toList());

        var statementListDepths =
                computeDepths(
                        statementList,
                        statementList.getElements(e -> e instanceof CtStatementList));
        CtStatementList deepestCommonParent =
                greedyFindDeepestCommonParent(statementListsWithVarAccess, statementListDepths);

        int firstStatementAccessingVarIdx =
                findFirstStatementAccessingVarIdx(deepestCommonParent, localVar);
        findDeclarationMergeableWrite(
                        varAccesses, deepestCommonParent, firstStatementAccessingVarIdx)
                .ifPresentOrElse(
                        this::makeDeclaration,
                        () -> {
                            CtLocalVariable<?> localVarWithoutInit = localVar.clone();
                            localVarWithoutInit.getAssignment().delete();
                            deepestCommonParent.addStatement(
                                    firstStatementAccessingVarIdx, localVarWithoutInit);
                        });
    }

    /**
     * We can turn a write into the variable declaration iff the write appears in a non-nested
     * statement in the common parent list, and is the first access to the processed variable that
     * appears in the common parent list.
     *
     * @param varAccesses All variable accesses to the considered variable.
     * @param commonParentList The common parent list.
     * @param firstStatementAccessingVarIdx Index of the first statement that accesses the
     *     considered variable (possibly nested access).
     * @return A write that is OK to merge with the variable declaration, or empty if no such write
     *     is found.
     */
    private Optional<CtVariableWrite<?>> findDeclarationMergeableWrite(
            List<CtVariableAccess<?>> varAccesses,
            CtStatementList commonParentList,
            int firstStatementAccessingVarIdx) {
        Optional<CtVariableAccess<?>> firstNonNestedVarAccessOpt =
                varAccesses.stream()
                        .filter(
                                access ->
                                        access.getParent(CtStatementList.class) == commonParentList)
                        .findFirst();

        if (firstNonNestedVarAccessOpt.isPresent()) {
            CtVariableAccess<?> firstNonNestedVarAccess = firstNonNestedVarAccessOpt.get();
            int firstAccessIdx =
                    commonParentList
                            .getStatements()
                            .indexOf(firstNonNestedVarAccess.getParent(CtStatement.class));
            return firstNonNestedVarAccess instanceof CtVariableWrite
                            && firstAccessIdx == firstStatementAccessingVarIdx
                    ? Optional.of((CtVariableWrite<?>) firstNonNestedVarAccess)
                    : Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    /**
     * @param statementList A statement list.
     * @param localVar A local variable declaration.
     * @return The index of the first statement that accesses the given variable. Note that the
     *     access can be arbitrarily deeply nested within the statement, if the statement is e.g. a
     *     block.
     */
    private int findFirstStatementAccessingVarIdx(
            CtStatementList statementList, CtLocalVariable<?> localVar) {
        for (int i = 0; i < statementList.getStatements().size(); i++) {
            var statement = statementList.getStatement(i);
            if (!statement.getElements(accessFilter(localVar)).isEmpty()) {
                return i;
            }
        }
        throw new IllegalStateException(
                "expected statement list to contain at least one access to " + localVar + "!");
    }

    private void makeDeclaration(CtVariableWrite<?> write) {
        CtVariable<?> decl = (CtVariable<?>) write.getVariable().getDeclaration().clone();
        CtAssignment assignment = write.getParent(CtAssignment.class);
        decl.setDefaultExpression(assignment.getAssignment());
        assignment.replace(decl);
    }

    /**
     * Find the statement list with the greatest depth that is the common parent of all provided
     * statement lists.
     *
     * <p>Note that the search for the deepest common parent is greedy. If there are two common
     * parents with the same depth, the first one found will be picked.
     *
     * @param statementLists A list of statement lists.
     * @param depths The depths of the statement lists, counted from any common parent.
     * @return A common parent of all given statement lists such that no other common parent has
     *     greater depth.
     */
    private CtStatementList greedyFindDeepestCommonParent(
            List<CtStatementList> statementLists, Map<CtElement, Integer> depths) {
        if (statementLists.size() == 1) {
            return statementLists.get(0);
        }

        return statementLists.stream()
                .reduce((lhs, rhs) -> greedyFindDeepestCommonParent(lhs, rhs, depths))
                .get();
    }

    /**
     * Find the deepest common parent list of the inputs, using the depths as a guide for the
     * search.
     */
    private CtStatementList greedyFindDeepestCommonParent(
            CtStatementList lhs, CtStatementList rhs, Map<CtElement, Integer> depths) {
        if (lhs == rhs) {
            return lhs;
        } else if (depths.get(lhs).equals(depths.get(rhs))) {
            return greedyFindDeepestCommonParent(
                    lhs.getParent(CtStatementList.class),
                    rhs.getParent(CtStatementList.class),
                    depths);
        } else if (depths.get(lhs) > depths.get(rhs)) {
            return greedyFindDeepestCommonParent(lhs.getParent(CtStatementList.class), rhs, depths);
        } else {
            return greedyFindDeepestCommonParent(lhs, rhs.getParent(CtStatementList.class), depths);
        }
    }

    private Map<CtElement, Integer> computeDepths(
            CtElement parent, List<? extends CtElement> children) {
        Map<CtElement, Integer> idMap = new IdentityHashMap<>();
        children.forEach(child -> idMap.putIfAbsent(child, depth(parent, child)));
        return idMap;
    }

    private int depth(CtElement parent, CtElement child) {
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
