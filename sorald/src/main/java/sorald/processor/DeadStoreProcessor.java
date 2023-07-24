package sorald.processor;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.meta.RoleHandler;
import spoon.reflect.meta.impl.RoleHandlerHelper;
import spoon.reflect.path.CtRole;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.Filter;

@ProcessorAnnotation(key = "S1854", description = "Unused assignments should be removed")
public class DeadStoreProcessor extends SoraldAbstractProcessor<CtStatement> {

    @Override
    protected boolean canRepairInternal(CtStatement candidate) {
        return candidate instanceof CtLocalVariable
                || candidate instanceof CtAssignment
                || candidate instanceof CtUnaryOperator;
    }

    @Override
    protected void repairInternal(CtStatement element) {
        if (element instanceof CtLocalVariable) {
            retainDeclarationOnVariableUse((CtLocalVariable<?>) element);
        }

        safeDeleteDeadStore(element);
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
        List<CtVariableAccess<?>> liveVarAccesses =
                statementList.getElements(liveAccessFilter(localVar));

        if (!liveVarAccesses.isEmpty()) {
            createNewDeclaration(statementList, liveVarAccesses, localVar);
        }
    }

    /**
     * Predicate that says "yes" only to live accesses to the given variable. That is to say, dead
     * stores are not included.
     */
    private Filter<CtVariableAccess<?>> liveAccessFilter(CtLocalVariable<?> localVar) {
        return (varAccess) -> {
            CtVariableReference<?> ref = varAccess.getVariable();
            return !isDeadStore(varAccess) && ref != null && ref.getDeclaration() == localVar;
        };
    }

    /**
     * @param varAccess Access to a variable.
     * @return true if the variable access is a dead store (according to the best fits mapping).
     */
    private boolean isDeadStore(CtVariableAccess<?> varAccess) {
        CtStatement parentStatement = varAccess.getParent(CtStatement.class);
        return getBestFits().containsKey(parentStatement);
    }

    /**
     * Given that there are variable accesses other than the initial dead store, we must create a
     * new declaration. This method does so with the tightest possible scope, merging the
     * declaration with a variable write if possible.
     *
     * @param statementList The statement list in which the variable declaration appears.
     * @param liveVarAccesses All non-dead-store accesses to the variable.
     * @param localVar The variable declaration itself.
     */
    private void createNewDeclaration(
            CtStatementList statementList,
            List<CtVariableAccess<?>> liveVarAccesses,
            CtLocalVariable<?> localVar) {
        List<CtStatementList> statementListsWithVarAccess =
                liveVarAccesses.stream()
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
                        liveVarAccesses, deepestCommonParent, firstStatementAccessingVarIdx)
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
     * @param liveVarAccesses All non-dead-store variable accesses to the considered variable.
     * @param commonParentList The common parent list.
     * @param firstStatementAccessingVarIdx Index of the first statement that accesses the
     *     considered variable (possibly nested access).
     * @return A write that is OK to merge with the variable declaration, or empty if no such write
     *     is found.
     */
    private Optional<CtVariableWrite<?>> findDeclarationMergeableWrite(
            List<CtVariableAccess<?>> liveVarAccesses,
            CtStatementList commonParentList,
            int firstStatementAccessingVarIdx) {
        Optional<CtVariableAccess<?>> firstNonNestedVarAccessOpt =
                liveVarAccesses.stream()
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
            if (!statement.getElements(liveAccessFilter(localVar)).isEmpty()) {
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

    /**
     * Delete an element that SonarJava has pointed out as a dead store.
     *
     * <p>If the element is an assignment or local variable where the assigned expression is a
     * non-static method invocation, then the invocation itself is left intact, but the assignment
     * to the variable (i.e. the dead store) is removed.
     *
     * @param element A dead store element to safe-delete
     */
    private static void safeDeleteDeadStore(CtElement element) {
        if (element.getRoleInParent() != CtRole.STATEMENT) {
            safeDeleteDeadStoreInExpression(element);
            return;
        } else if (element instanceof CtUnaryOperator) {
            // unary operator in statement position: must be e.g. ++x, which we can just delete
            element.delete();
            return;
        }

        CtElement assignment =
                element instanceof CtLocalVariable
                        ? ((CtLocalVariable<?>) element).getAssignment()
                        : ((CtAssignment<?, ?>) element).getAssignment();

        // Important: As Spoon can't always resolve method references, we should only delete an
        // element if we can definitively say that it _is_ a static method invocation (rather
        // than check if it's not an instance method invocation), or that it's not a method
        // invocation
        // at all. This is the "safest" approach.
        if (!(assignment instanceof CtInvocation) || isStaticMethodInvocation(assignment)) {
            element.delete();
        } else {
            element.replace(assignment);
        }
    }

    private static boolean isStaticMethodInvocation(CtElement element) {
        if (!(element instanceof CtInvocation)
                || ((CtInvocation<?>) element).getExecutable() == null
                || ((CtInvocation<?>) element).getExecutable().getExecutableDeclaration() == null) {
            return false;
        }
        CtExecutable<?> exec =
                ((CtInvocation<?>) element).getExecutable().getExecutableDeclaration();
        return exec instanceof CtMethod && ((CtMethod<?>) exec).isStatic();
    }

    /**
     * We've got a dead store inside of an expression, meaning a suffix unary operator or an
     * expression assignment. Sometimes, the store is dead but the value is still read, and then the
     * returned value must be retained while the store is removed.
     */
    private static void safeDeleteDeadStoreInExpression(CtElement element) {
        CtElement replacement = extractDeadStoreStatementExpressionReplacement(element);
        if (isApplicableForRoleInParent(
                replacement, element.getRoleInParent(), element.getParent())) {
            element.replace(replacement);
        } else {
            element.delete();
        }
    }

    /**
     * When there is a dead store in a statement expression from which the value is read, the dead
     * store needs to be replaced with an expression that only returns the value, without causing a
     * store. This method extracts that value.
     */
    private static CtElement extractDeadStoreStatementExpressionReplacement(CtElement element) {
        if (element instanceof CtAssignment) {
            // in an expression assignment, we need to keep the assignment (the RHS),
            // but not the reference to the assigned variable (the LHS)
            return ((CtAssignment<?, ?>) element).getAssignment();
        } else if (element instanceof CtUnaryOperator) {
            // This must be a postfix or suffix operator that mutates the variable,
            // which always contains a variable write in Spoon.
            CtVariableWrite<?> varWrite =
                    (CtVariableWrite<?>) ((CtUnaryOperator<?>) element).getOperand();
            CtVariableRead<?> replacement = convertToVarRead(varWrite);
            return replacement;
        } else {
            throw new IllegalArgumentException(
                    "unexpected element type: " + element.getClass().getName());
        }
    }

    /** Check whether the child can be assigned to the role in the given parent. */
    private static boolean isApplicableForRoleInParent(
            CtElement child, CtRole roleInParent, CtElement parent) {
        RoleHandler parentRoleHandler =
                RoleHandlerHelper.getRoleHandler(parent.getClass(), roleInParent);
        Class<?> classForRole = parentRoleHandler.getValueClass();
        return classForRole.isAssignableFrom(child.getClass());
    }

    private static <T> CtVariableRead<T> convertToVarRead(CtVariableWrite<T> varWrite) {
        CtVariableRead<T> varRead = varWrite.getFactory().createVariableRead();
        for (CtElement child : varWrite.getDirectChildren()) {
            varRead.setValueByRole(child.getRoleInParent(), child.clone());
        }
        return varRead;
    }
}
