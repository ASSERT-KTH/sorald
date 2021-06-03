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
public class UnclosedResourcesProcessor extends SoraldAbstractProcessor<CtConstructorCall> {

    @Override
    protected void repairInternal(CtConstructorCall element) {
        CtElement parent =
                element.getParent(e -> e instanceof CtAssignment || e instanceof CtLocalVariable);

        if (parent instanceof CtLocalVariable) {
            CtLocalVariable ctLocalVariable = ((CtLocalVariable) parent);
            createCtTryWithResource(parent, ctLocalVariable.clone());
        } else if (parent instanceof CtAssignment) {
            CtAssignment ctAssignment = (CtAssignment) parent;
            CtExpression expressionAssigned = ctAssignment.getAssigned();

            if (expressionAssigned instanceof CtVariableWrite) {
                CtVariableWrite ctVariableWrite = (CtVariableWrite) expressionAssigned;
                CtVariableReference ctVariableReference = ctVariableWrite.getVariable();
                if (ctVariableReference.getDeclaration() instanceof CtLocalVariable) {
                    CtLocalVariable ctLocalVariable =
                            (CtLocalVariable) ctVariableReference.getDeclaration();
                    CtLocalVariable clonedCtLocalVariable = ctLocalVariable.clone();
                    clonedCtLocalVariable.setAssignment(ctAssignment.getAssignment().clone());
                    ctLocalVariable.delete();
                    createCtTryWithResource(parent, clonedCtLocalVariable);
                }
            }
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
     * Enclose the unclosed resource, and everything that comes after it, into a try block placed
     * inside the provided try-with-resource.
     */
    private void encloseResourceInTryBlock(
            CtStatement unclosedResourceStatement, CtTryWithResource tryWithResource) {
        CtBlock<?> enclosingBlock = unclosedResourceStatement.getParent(CtBlock.class);
        CtBlock<?> tryBlock = getFactory().createBlock();
        int unclosedResourceStatementIdx =
                enclosingBlock.getStatements().indexOf(unclosedResourceStatement);
        List<CtStatement> enclosingBlockStatements = List.copyOf(enclosingBlock.getStatements());
        List<CtStatement> statementsToMove =
                enclosingBlockStatements.subList(
                        unclosedResourceStatementIdx, enclosingBlockStatements.size());

        for (CtStatement statement : statementsToMove) {
            statement.delete();
            tryBlock.addStatement(statement.clone());
        }

        tryWithResource.setBody(tryBlock);
        unclosedResourceStatement.replace(tryWithResource);
    }
}
