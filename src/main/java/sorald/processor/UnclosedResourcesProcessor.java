package sorald.processor;

import java.util.List;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtTry;
import spoon.reflect.code.CtTryWithResource;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.declaration.CtElement;
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
        } else if (parent instanceof CtAssignment) {
            refactorAssignmentIntoTryWithResources((CtAssignment<?, ?>) parent);
        }
    }

    private void createCtTryWithResource(CtElement parent, CtLocalVariable variable) {
        CtTryWithResource tryWithResource = getFactory().createTryWithResource();
        tryWithResource.addResource(variable);

        CtBlock parentCtBlock = parent.getParent(CtBlock.class);
        boolean isInTry = parentCtBlock.getParent() instanceof CtTry;
        boolean isInTryWithResource = parentCtBlock.getParent() instanceof CtTryWithResource;
        if (isInTryWithResource) {
            ((CtTryWithResource) parentCtBlock.getParent()).addResource(variable);
            parent.delete();
        } else if (isInTry) {
            parent.delete();
            tryWithResource.setCatchers(((CtTry) parentCtBlock.getParent()).getCatchers());
            parentCtBlock.getParent().replace(tryWithResource);
            tryWithResource.setBody(parentCtBlock);
        } else {
            encloseResourceInTryBlock((CtStatement) parent, tryWithResource);
        }
    }

    /**
     * When the unclosed resource is initialized in an assignment rather than in a local variable
     * declaration, we must locate the declaration and move both declaration and assignment into the
     * resource list.
     */
    private <T, A extends T> void refactorAssignmentIntoTryWithResources(
            CtAssignment<T, A> assignment) {
        CtExpression<T> expressionAssigned = assignment.getAssigned();
        if (expressionAssigned instanceof CtVariableWrite) {
            CtVariableWrite<T> ctVariableWrite = (CtVariableWrite<T>) expressionAssigned;
            CtVariableReference<T> ctVariableReference = ctVariableWrite.getVariable();
            if (ctVariableReference.getDeclaration() instanceof CtLocalVariable) {
                CtLocalVariable<A> ctLocalVariable =
                        (CtLocalVariable<A>) ctVariableReference.getDeclaration();
                CtLocalVariable<A> clonedCtLocalVariable = ctLocalVariable.clone();
                clonedCtLocalVariable.setAssignment(assignment.getAssignment().clone());
                ctLocalVariable.delete();
                createCtTryWithResource(assignment, clonedCtLocalVariable);
            }
        }
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
}
