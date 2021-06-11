package sorald.processor;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtTry;
import spoon.reflect.code.CtTryWithResource;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.reference.CtVariableReference;

@ProcessorAnnotation(key = 2095, description = "Resources should be closed")
public class UnclosedResourcesProcessor extends SoraldAbstractProcessor<CtConstructorCall<?>> {

    @Override
    protected void repairInternal(CtConstructorCall<?> element) {
        CtElement parent =
                element.getParent(e -> e instanceof CtAssignment || e instanceof CtLocalVariable);

        if (parent instanceof CtLocalVariable) {
            CtLocalVariable<?> ctLocalVariable = ((CtLocalVariable<?>) parent);
            createCtTryWithResource(parent, ctLocalVariable.clone());
        } else if (isAssignmentWithWriteToResolvedLocalVariable(parent)) {
            refactorLocalVariableWriteIntoTryWithResources((CtAssignment<?, ?>) parent);
        }
    }

    private void createCtTryWithResource(CtElement parent, CtLocalVariable<?> variable) {
        CtTryWithResource tryWithResource = getFactory().createTryWithResource();
        tryWithResource.addResource(variable);

        CtBlock<?> enclosingBlock = parent.getParent(CtBlock.class);
        CtElement enclosingBlockParent = enclosingBlock.getParent();
        if (enclosingBlockParent instanceof CtTryWithResource) {
            ((CtTryWithResource) enclosingBlockParent).addResource(variable);
            parent.delete();
        } else if (enclosingBlockParent instanceof CtTry) {
            CtTry enclosingTry = (CtTry) enclosingBlockParent;
            tryWithResource.setCatchers(enclosingTry.getCatchers());
            tryWithResource.setBody(enclosingBlock);
            tryWithResource.setFinalizer(enclosingTry.getFinalizer());
            enclosingTry.replace(tryWithResource);
            parent.delete();
        } else {
            encloseResourceInTryBlock((CtStatement) parent, tryWithResource);
        }

        cleanFinalizerAndCatchersOfReferencesToLocalVariableWithName(
                tryWithResource, variable.getSimpleName());
    }

    /**
     * When the unclosed resource is initialized in an assignment rather than in a local variable
     * declaration, we must locate the declaration and move both declaration and assignment into the
     * resource list.
     *
     * <p>This requires that the expression assigned to is a variable write to a local variable.
     */
    private <T, A extends T> void refactorLocalVariableWriteIntoTryWithResources(
            CtAssignment<T, A> assignment) {
        CtExpression<T> expressionAssigned = assignment.getAssigned();
        CtVariableWrite<T> variableWrite = (CtVariableWrite<T>) expressionAssigned;
        CtVariableReference<T> variableReference = variableWrite.getVariable();

        CtLocalVariable<A> localVariable = (CtLocalVariable<A>) variableReference.getDeclaration();
        CtLocalVariable<A> localVariableClone = localVariable.clone();
        localVariableClone.setAssignment(assignment.getAssignment().clone());
        localVariable.delete();
        createCtTryWithResource(assignment, localVariableClone);
    }

    private boolean isAssignmentWithWriteToResolvedLocalVariable(CtElement element) {
        return element instanceof CtAssignment
                && isWriteToResolvedLocalVariable(((CtAssignment<?, ?>) element).getAssigned());
    }

    private boolean isWriteToResolvedLocalVariable(CtExpression<?> expression) {
        return (expression instanceof CtVariableWrite)
                && ((CtVariableWrite<?>) expression).getVariable().getDeclaration()
                        instanceof CtLocalVariable;
    }

    /**
     * Enclose the unclosed resource, and everything that comes after it, into a try block placed
     * inside the provided try-with-resource.
     */
    private void encloseResourceInTryBlock(
            CtStatement unclosedResourceStatement, CtTryWithResource tryWithResource) {
        CtBlock<?> enclosingBlock = unclosedResourceStatement.getParent(CtBlock.class);
        int firstStatementToMoveIdx =
                enclosingBlock.getStatements().indexOf(unclosedResourceStatement);
        CtBlock<?> tryBlock =
                moveStatementsToNewBlock(enclosingBlock.getStatements(), firstStatementToMoveIdx);
        tryWithResource.setBody(tryBlock);
        unclosedResourceStatement.replace(tryWithResource);
    }

    /**
     * Move all statements starting from the firstStatementToMove to a new block and return that
     * block.
     */
    private CtBlock<?> moveStatementsToNewBlock(
            List<CtStatement> statements, int firstStatementToMove) {
        var statementsToMove = statements.subList(firstStatementToMove, statements.size());
        CtBlock<?> block = getFactory().createBlock();
        for (var statement : List.copyOf(statementsToMove)) {
            statement.delete();
            block.addStatement(statement.clone());
        }
        return block;
    }

    private void cleanFinalizerAndCatchersOfReferencesToLocalVariableWithName(
            CtTry ctTry, String variableName) {
        var blocksToClean =
                Stream.concat(
                                Stream.of(ctTry.getFinalizer()),
                                ctTry.getCatchers().stream().map(CtCatch::getBody))
                        .filter(Objects::nonNull);
        blocksToClean.forEach(
                block ->
                        deleteStatementsInBlockThatReferenceLocalVariableWithName(
                                block, variableName));
    }

    private void deleteStatementsInBlockThatReferenceLocalVariableWithName(
            CtBlock<?> block, String variableName) {
        List<CtVariableReference<?>> varRefs =
                block.filterChildren(CtLocalVariableReference.class::isInstance).list();

        varRefs.stream()
                .filter(ref -> variableName.equals(ref.getSimpleName()))
                .map(ref -> ref.getParent(CtStatement.class))
                .forEach(CtStatement::delete);
    }
}
