package sorald.processor;

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

    private void encloseResourceInTryBlock(
            CtStatement unclosedResourceStatement, CtTryWithResource tryWithResource) {
        CtBlock<?> enclosingBlock = unclosedResourceStatement.getParent(CtBlock.class);
        CtBlock<?> tryBlock = getFactory().createBlock();
        int unclosedResourceStatementIdx = -1;

        for (int i = 0; i < enclosingBlock.getStatements().size(); i++) {
            CtStatement statement = enclosingBlock.getStatements().get(i);
            if (statement == unclosedResourceStatement) {
                unclosedResourceStatementIdx = i + 1;
                continue;
            }
            if (unclosedResourceStatementIdx != -1) {
                tryBlock.addStatement(statement.clone());
            }
        }

        int numStatements = enclosingBlock.getStatements().size();
        if (unclosedResourceStatementIdx != -1) {
            for (int i = unclosedResourceStatementIdx; i < numStatements; i++) {
                enclosingBlock.getStatement(unclosedResourceStatementIdx).delete();
            }
        }

        tryWithResource.setBody(tryBlock);
        unclosedResourceStatement.replace(tryWithResource);
    }
}
