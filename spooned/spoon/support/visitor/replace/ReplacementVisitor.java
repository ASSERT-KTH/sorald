/**
 * Copyright (C) 2006-2017 INRIA and contributors
 * Spoon - http://spoon.gforge.inria.fr/
 *
 * This software is governed by the CeCILL-C License under French law and
 * abiding by the rules of distribution of free software. You can use, modify
 * and/or redistribute the software under the terms of the CeCILL-C license as
 * circulated by CEA, CNRS and INRIA at http://www.cecill.info.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the CeCILL-C License for more details.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package spoon.support.visitor.replace;


import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import spoon.SpoonException;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtAnnotationFieldAccess;
import spoon.reflect.code.CtArrayAccess;
import spoon.reflect.code.CtArrayRead;
import spoon.reflect.code.CtArrayWrite;
import spoon.reflect.code.CtAssert;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtBodyHolder;
import spoon.reflect.code.CtBreak;
import spoon.reflect.code.CtCase;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtCatchVariable;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtContinue;
import spoon.reflect.code.CtDo;
import spoon.reflect.code.CtExecutableReferenceExpression;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtJavaDoc;
import spoon.reflect.code.CtJavaDocTag;
import spoon.reflect.code.CtLambda;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.code.CtOperatorAssignment;
import spoon.reflect.code.CtRHSReceiver;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtSuperAccess;
import spoon.reflect.code.CtSwitch;
import spoon.reflect.code.CtSynchronized;
import spoon.reflect.code.CtTargetedExpression;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtThrow;
import spoon.reflect.code.CtTry;
import spoon.reflect.code.CtTryWithResource;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.code.CtWhile;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtAnnotationMethod;
import spoon.reflect.declaration.CtAnnotationType;
import spoon.reflect.declaration.CtAnonymousExecutable;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtEnum;
import spoon.reflect.declaration.CtEnumValue;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtFormalTypeDeclarer;
import spoon.reflect.declaration.CtImport;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtModule;
import spoon.reflect.declaration.CtModuleRequirement;
import spoon.reflect.declaration.CtMultiTypedElement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtPackageExport;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtProvidedService;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeParameter;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.declaration.CtUsedService;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtActualTypeContainer;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtCatchVariableReference;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtIntersectionTypeReference;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.reference.CtModuleReference;
import spoon.reflect.reference.CtPackageReference;
import spoon.reflect.reference.CtParameterReference;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeParameterReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtUnboundVariableReference;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.reference.CtWildcardReference;
import spoon.reflect.visitor.CtScanner;


/**
 * Used to replace an element by another one.
 *
 * This class is generated automatically by the processor spoon.generating.ReplacementVisitorGenerator.
 */
public class ReplacementVisitor extends CtScanner {
    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtTypedElementTypeReplaceListener implements ReplaceListener<CtTypeReference> {
        private final CtTypedElement element;

        CtTypedElementTypeReplaceListener(CtTypedElement element) {
            this.element = element;
        }

        @Override
        public void set(CtTypeReference replace) {
            this.element.setType(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtElementCommentsReplaceListener implements ReplaceListListener<List> {
        private final CtElement element;

        CtElementCommentsReplaceListener(CtElement element) {
            this.element = element;
        }

        @Override
        public void set(List replace) {
            this.element.setComments(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtAnnotationAnnotationTypeReplaceListener implements ReplaceListener<CtTypeReference> {
        private final CtAnnotation element;

        CtAnnotationAnnotationTypeReplaceListener(CtAnnotation element) {
            this.element = element;
        }

        @Override
        public void set(CtTypeReference replace) {
            this.element.setAnnotationType(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtElementAnnotationsReplaceListener implements ReplaceListListener<List> {
        private final CtElement element;

        CtElementAnnotationsReplaceListener(CtElement element) {
            this.element = element;
        }

        @Override
        public void set(List replace) {
            this.element.setAnnotations(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtAnnotationValuesReplaceListener implements ReplaceMapListener<Map> {
        private final CtAnnotation element;

        CtAnnotationValuesReplaceListener(CtAnnotation element) {
            this.element = element;
        }

        @Override
        public void set(Map replace) {
            this.element.setValues(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtTypeTypeMembersReplaceListener implements ReplaceListListener<List> {
        private final CtType element;

        CtTypeTypeMembersReplaceListener(CtType element) {
            this.element = element;
        }

        @Override
        public void set(List replace) {
            this.element.setTypeMembers(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtExecutableBodyReplaceListener implements ReplaceListener<CtBlock> {
        private final CtBodyHolder element;

        CtExecutableBodyReplaceListener(CtBodyHolder element) {
            this.element = element;
        }

        @Override
        public void set(CtBlock replace) {
            this.element.setBody(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtExpressionTypeCastsReplaceListener implements ReplaceListListener<List> {
        private final CtExpression element;

        CtExpressionTypeCastsReplaceListener(CtExpression element) {
            this.element = element;
        }

        @Override
        public void set(List replace) {
            this.element.setTypeCasts(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtTargetedExpressionTargetReplaceListener implements ReplaceListener<CtExpression> {
        private final CtTargetedExpression element;

        CtTargetedExpressionTargetReplaceListener(CtTargetedExpression element) {
            this.element = element;
        }

        @Override
        public void set(CtExpression replace) {
            this.element.setTarget(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtArrayAccessIndexExpressionReplaceListener implements ReplaceListener<CtExpression> {
        private final CtArrayAccess element;

        CtArrayAccessIndexExpressionReplaceListener(CtArrayAccess element) {
            this.element = element;
        }

        @Override
        public void set(CtExpression replace) {
            this.element.setIndexExpression(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtTypeReferencePackageReplaceListener implements ReplaceListener<CtPackageReference> {
        private final CtTypeReference element;

        CtTypeReferencePackageReplaceListener(CtTypeReference element) {
            this.element = element;
        }

        @Override
        public void set(CtPackageReference replace) {
            this.element.setPackage(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtTypeReferenceDeclaringTypeReplaceListener implements ReplaceListener<CtTypeReference> {
        private final CtTypeReference element;

        CtTypeReferenceDeclaringTypeReplaceListener(CtTypeReference element) {
            this.element = element;
        }

        @Override
        public void set(CtTypeReference replace) {
            this.element.setDeclaringType(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtArrayTypeReferenceComponentTypeReplaceListener implements ReplaceListener<CtTypeReference> {
        private final CtArrayTypeReference element;

        CtArrayTypeReferenceComponentTypeReplaceListener(CtArrayTypeReference element) {
            this.element = element;
        }

        @Override
        public void set(CtTypeReference replace) {
            this.element.setComponentType(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtActualTypeContainerActualTypeArgumentsReplaceListener implements ReplaceListListener<List> {
        private final CtActualTypeContainer element;

        CtActualTypeContainerActualTypeArgumentsReplaceListener(CtActualTypeContainer element) {
            this.element = element;
        }

        @Override
        public void set(List replace) {
            this.element.setActualTypeArguments(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtAssertAssertExpressionReplaceListener implements ReplaceListener<CtExpression> {
        private final CtAssert element;

        CtAssertAssertExpressionReplaceListener(CtAssert element) {
            this.element = element;
        }

        @Override
        public void set(CtExpression replace) {
            this.element.setAssertExpression(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtAssertExpressionReplaceListener implements ReplaceListener<CtExpression> {
        private final CtAssert element;

        CtAssertExpressionReplaceListener(CtAssert element) {
            this.element = element;
        }

        @Override
        public void set(CtExpression replace) {
            this.element.setExpression(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtAssignmentAssignedReplaceListener implements ReplaceListener<CtExpression> {
        private final CtAssignment element;

        CtAssignmentAssignedReplaceListener(CtAssignment element) {
            this.element = element;
        }

        @Override
        public void set(CtExpression replace) {
            this.element.setAssigned(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtRHSReceiverAssignmentReplaceListener implements ReplaceListener<CtExpression> {
        private final CtRHSReceiver element;

        CtRHSReceiverAssignmentReplaceListener(CtRHSReceiver element) {
            this.element = element;
        }

        @Override
        public void set(CtExpression replace) {
            this.element.setAssignment(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtBinaryOperatorLeftHandOperandReplaceListener implements ReplaceListener<CtExpression> {
        private final CtBinaryOperator element;

        CtBinaryOperatorLeftHandOperandReplaceListener(CtBinaryOperator element) {
            this.element = element;
        }

        @Override
        public void set(CtExpression replace) {
            this.element.setLeftHandOperand(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtBinaryOperatorRightHandOperandReplaceListener implements ReplaceListener<CtExpression> {
        private final CtBinaryOperator element;

        CtBinaryOperatorRightHandOperandReplaceListener(CtBinaryOperator element) {
            this.element = element;
        }

        @Override
        public void set(CtExpression replace) {
            this.element.setRightHandOperand(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtStatementListStatementsReplaceListener implements ReplaceListListener<List> {
        private final CtStatementList element;

        CtStatementListStatementsReplaceListener(CtStatementList element) {
            this.element = element;
        }

        @Override
        public void set(List replace) {
            this.element.setStatements(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtCaseCaseExpressionReplaceListener implements ReplaceListener<CtExpression> {
        private final CtCase element;

        CtCaseCaseExpressionReplaceListener(CtCase element) {
            this.element = element;
        }

        @Override
        public void set(CtExpression replace) {
            this.element.setCaseExpression(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtCatchParameterReplaceListener implements ReplaceListener<CtCatchVariable> {
        private final CtCatch element;

        CtCatchParameterReplaceListener(CtCatch element) {
            this.element = element;
        }

        @Override
        public void set(CtCatchVariable replace) {
            this.element.setParameter(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtCatchBodyReplaceListener implements ReplaceListener<CtBlock> {
        private final CtBodyHolder element;

        CtCatchBodyReplaceListener(CtBodyHolder element) {
            this.element = element;
        }

        @Override
        public void set(CtBlock replace) {
            this.element.setBody(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtTypeInformationSuperclassReplaceListener implements ReplaceListener<CtTypeReference> {
        private final CtType element;

        CtTypeInformationSuperclassReplaceListener(CtType element) {
            this.element = element;
        }

        @Override
        public void set(CtTypeReference replace) {
            this.element.setSuperclass(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtTypeInformationSuperInterfacesReplaceListener implements ReplaceSetListener<Set> {
        private final CtType element;

        CtTypeInformationSuperInterfacesReplaceListener(CtType element) {
            this.element = element;
        }

        @Override
        public void set(Set replace) {
            this.element.setSuperInterfaces(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtFormalTypeDeclarerFormalCtTypeParametersReplaceListener implements ReplaceListListener<List> {
        private final CtFormalTypeDeclarer element;

        CtFormalTypeDeclarerFormalCtTypeParametersReplaceListener(CtFormalTypeDeclarer element) {
            this.element = element;
        }

        @Override
        public void set(List replace) {
            this.element.setFormalCtTypeParameters(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtConditionalConditionReplaceListener implements ReplaceListener<CtExpression> {
        private final CtConditional element;

        CtConditionalConditionReplaceListener(CtConditional element) {
            this.element = element;
        }

        @Override
        public void set(CtExpression replace) {
            this.element.setCondition(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtConditionalThenExpressionReplaceListener implements ReplaceListener<CtExpression> {
        private final CtConditional element;

        CtConditionalThenExpressionReplaceListener(CtConditional element) {
            this.element = element;
        }

        @Override
        public void set(CtExpression replace) {
            this.element.setThenExpression(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtConditionalElseExpressionReplaceListener implements ReplaceListener<CtExpression> {
        private final CtConditional element;

        CtConditionalElseExpressionReplaceListener(CtConditional element) {
            this.element = element;
        }

        @Override
        public void set(CtExpression replace) {
            this.element.setElseExpression(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtExecutableParametersReplaceListener implements ReplaceListListener<List> {
        private final CtExecutable element;

        CtExecutableParametersReplaceListener(CtExecutable element) {
            this.element = element;
        }

        @Override
        public void set(List replace) {
            this.element.setParameters(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtExecutableThrownTypesReplaceListener implements ReplaceSetListener<Set> {
        private final CtExecutable element;

        CtExecutableThrownTypesReplaceListener(CtExecutable element) {
            this.element = element;
        }

        @Override
        public void set(Set replace) {
            this.element.setThrownTypes(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtDoLoopingExpressionReplaceListener implements ReplaceListener<CtExpression> {
        private final CtDo element;

        CtDoLoopingExpressionReplaceListener(CtDo element) {
            this.element = element;
        }

        @Override
        public void set(CtExpression replace) {
            this.element.setLoopingExpression(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtLoopBodyReplaceListener implements ReplaceListener<CtStatement> {
        private final CtBodyHolder element;

        CtLoopBodyReplaceListener(CtBodyHolder element) {
            this.element = element;
        }

        @Override
        public void set(CtStatement replace) {
            this.element.setBody(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtEnumEnumValuesReplaceListener implements ReplaceListListener<List> {
        private final CtEnum element;

        CtEnumEnumValuesReplaceListener(CtEnum element) {
            this.element = element;
        }

        @Override
        public void set(List replace) {
            this.element.setEnumValues(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtExecutableReferenceDeclaringTypeReplaceListener implements ReplaceListener<CtTypeReference> {
        private final CtExecutableReference element;

        CtExecutableReferenceDeclaringTypeReplaceListener(CtExecutableReference element) {
            this.element = element;
        }

        @Override
        public void set(CtTypeReference replace) {
            this.element.setDeclaringType(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtExecutableReferenceTypeReplaceListener implements ReplaceListener<CtTypeReference> {
        private final CtExecutableReference element;

        CtExecutableReferenceTypeReplaceListener(CtExecutableReference element) {
            this.element = element;
        }

        @Override
        public void set(CtTypeReference replace) {
            this.element.setType(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtExecutableReferenceParametersReplaceListener implements ReplaceListListener<List> {
        private final CtExecutableReference element;

        CtExecutableReferenceParametersReplaceListener(CtExecutableReference element) {
            this.element = element;
        }

        @Override
        public void set(List replace) {
            this.element.setParameters(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtVariableDefaultExpressionReplaceListener implements ReplaceListener<CtExpression> {
        private final CtVariable element;

        CtVariableDefaultExpressionReplaceListener(CtVariable element) {
            this.element = element;
        }

        @Override
        public void set(CtExpression replace) {
            this.element.setDefaultExpression(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtAnnotationFieldAccessVariableReplaceListener implements ReplaceListener<CtFieldReference> {
        private final CtVariableAccess element;

        CtAnnotationFieldAccessVariableReplaceListener(CtVariableAccess element) {
            this.element = element;
        }

        @Override
        public void set(CtFieldReference replace) {
            this.element.setVariable(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtFieldReferenceDeclaringTypeReplaceListener implements ReplaceListener<CtTypeReference> {
        private final CtFieldReference element;

        CtFieldReferenceDeclaringTypeReplaceListener(CtFieldReference element) {
            this.element = element;
        }

        @Override
        public void set(CtTypeReference replace) {
            this.element.setDeclaringType(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtVariableReferenceTypeReplaceListener implements ReplaceListener<CtTypeReference> {
        private final CtVariableReference element;

        CtVariableReferenceTypeReplaceListener(CtVariableReference element) {
            this.element = element;
        }

        @Override
        public void set(CtTypeReference replace) {
            this.element.setType(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtForForInitReplaceListener implements ReplaceListListener<List> {
        private final CtFor element;

        CtForForInitReplaceListener(CtFor element) {
            this.element = element;
        }

        @Override
        public void set(List replace) {
            this.element.setForInit(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtForExpressionReplaceListener implements ReplaceListener<CtExpression> {
        private final CtFor element;

        CtForExpressionReplaceListener(CtFor element) {
            this.element = element;
        }

        @Override
        public void set(CtExpression replace) {
            this.element.setExpression(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtForForUpdateReplaceListener implements ReplaceListListener<List> {
        private final CtFor element;

        CtForForUpdateReplaceListener(CtFor element) {
            this.element = element;
        }

        @Override
        public void set(List replace) {
            this.element.setForUpdate(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtForEachVariableReplaceListener implements ReplaceListener<CtLocalVariable> {
        private final CtForEach element;

        CtForEachVariableReplaceListener(CtForEach element) {
            this.element = element;
        }

        @Override
        public void set(CtLocalVariable replace) {
            this.element.setVariable(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtForEachExpressionReplaceListener implements ReplaceListener<CtExpression> {
        private final CtForEach element;

        CtForEachExpressionReplaceListener(CtForEach element) {
            this.element = element;
        }

        @Override
        public void set(CtExpression replace) {
            this.element.setExpression(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtIfConditionReplaceListener implements ReplaceListener<CtExpression> {
        private final CtIf element;

        CtIfConditionReplaceListener(CtIf element) {
            this.element = element;
        }

        @Override
        public void set(CtExpression replace) {
            this.element.setCondition(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtIfThenStatementReplaceListener implements ReplaceListener<CtStatement> {
        private final CtIf element;

        CtIfThenStatementReplaceListener(CtIf element) {
            this.element = element;
        }

        @Override
        public void set(CtStatement replace) {
            this.element.setThenStatement(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtIfElseStatementReplaceListener implements ReplaceListener<CtStatement> {
        private final CtIf element;

        CtIfElseStatementReplaceListener(CtIf element) {
            this.element = element;
        }

        @Override
        public void set(CtStatement replace) {
            this.element.setElseStatement(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtAbstractInvocationExecutableReplaceListener implements ReplaceListener<CtExecutableReference> {
        private final CtAbstractInvocation element;

        CtAbstractInvocationExecutableReplaceListener(CtAbstractInvocation element) {
            this.element = element;
        }

        @Override
        public void set(CtExecutableReference replace) {
            this.element.setExecutable(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtAbstractInvocationArgumentsReplaceListener implements ReplaceListListener<List> {
        private final CtAbstractInvocation element;

        CtAbstractInvocationArgumentsReplaceListener(CtAbstractInvocation element) {
            this.element = element;
        }

        @Override
        public void set(List replace) {
            this.element.setArguments(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtMultiTypedElementMultiTypesReplaceListener implements ReplaceListListener<List> {
        private final CtMultiTypedElement element;

        CtMultiTypedElementMultiTypesReplaceListener(CtMultiTypedElement element) {
            this.element = element;
        }

        @Override
        public void set(List replace) {
            this.element.setMultiTypes(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtAnnotationMethodDefaultExpressionReplaceListener implements ReplaceListener<CtExpression> {
        private final CtAnnotationMethod element;

        CtAnnotationMethodDefaultExpressionReplaceListener(CtAnnotationMethod element) {
            this.element = element;
        }

        @Override
        public void set(CtExpression replace) {
            this.element.setDefaultExpression(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtNewArrayElementsReplaceListener implements ReplaceListListener<List> {
        private final CtNewArray element;

        CtNewArrayElementsReplaceListener(CtNewArray element) {
            this.element = element;
        }

        @Override
        public void set(List replace) {
            this.element.setElements(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtNewArrayDimensionExpressionsReplaceListener implements ReplaceListListener<List> {
        private final CtNewArray element;

        CtNewArrayDimensionExpressionsReplaceListener(CtNewArray element) {
            this.element = element;
        }

        @Override
        public void set(List replace) {
            this.element.setDimensionExpressions(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtNewClassAnonymousClassReplaceListener implements ReplaceListener<CtClass> {
        private final CtNewClass element;

        CtNewClassAnonymousClassReplaceListener(CtNewClass element) {
            this.element = element;
        }

        @Override
        public void set(CtClass replace) {
            this.element.setAnonymousClass(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtLambdaExpressionReplaceListener implements ReplaceListener<CtExpression> {
        private final CtLambda element;

        CtLambdaExpressionReplaceListener(CtLambda element) {
            this.element = element;
        }

        @Override
        public void set(CtExpression replace) {
            this.element.setExpression(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtExecutableReferenceExpressionExecutableReplaceListener implements ReplaceListener<CtExecutableReference> {
        private final CtExecutableReferenceExpression element;

        CtExecutableReferenceExpressionExecutableReplaceListener(CtExecutableReferenceExpression element) {
            this.element = element;
        }

        @Override
        public void set(CtExecutableReference replace) {
            this.element.setExecutable(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtPackagePackagesReplaceListener implements ReplaceSetListener<Set> {
        private final CtPackage element;

        CtPackagePackagesReplaceListener(CtPackage element) {
            this.element = element;
        }

        @Override
        public void set(Set replace) {
            this.element.setPackages(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtPackageTypesReplaceListener implements ReplaceSetListener<Set> {
        private final CtPackage element;

        CtPackageTypesReplaceListener(CtPackage element) {
            this.element = element;
        }

        @Override
        public void set(Set replace) {
            this.element.setTypes(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtReturnReturnedExpressionReplaceListener implements ReplaceListener<CtExpression> {
        private final CtReturn element;

        CtReturnReturnedExpressionReplaceListener(CtReturn element) {
            this.element = element;
        }

        @Override
        public void set(CtExpression replace) {
            this.element.setReturnedExpression(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtSwitchSelectorReplaceListener implements ReplaceListener<CtExpression> {
        private final CtSwitch element;

        CtSwitchSelectorReplaceListener(CtSwitch element) {
            this.element = element;
        }

        @Override
        public void set(CtExpression replace) {
            this.element.setSelector(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtSwitchCasesReplaceListener implements ReplaceListListener<List> {
        private final CtSwitch element;

        CtSwitchCasesReplaceListener(CtSwitch element) {
            this.element = element;
        }

        @Override
        public void set(List replace) {
            this.element.setCases(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtSynchronizedExpressionReplaceListener implements ReplaceListener<CtExpression> {
        private final CtSynchronized element;

        CtSynchronizedExpressionReplaceListener(CtSynchronized element) {
            this.element = element;
        }

        @Override
        public void set(CtExpression replace) {
            this.element.setExpression(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtSynchronizedBlockReplaceListener implements ReplaceListener<CtBlock> {
        private final CtSynchronized element;

        CtSynchronizedBlockReplaceListener(CtSynchronized element) {
            this.element = element;
        }

        @Override
        public void set(CtBlock replace) {
            this.element.setBlock(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtThrowThrownExpressionReplaceListener implements ReplaceListener<CtExpression> {
        private final CtThrow element;

        CtThrowThrownExpressionReplaceListener(CtThrow element) {
            this.element = element;
        }

        @Override
        public void set(CtExpression replace) {
            this.element.setThrownExpression(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtTryBodyReplaceListener implements ReplaceListener<CtBlock> {
        private final CtBodyHolder element;

        CtTryBodyReplaceListener(CtBodyHolder element) {
            this.element = element;
        }

        @Override
        public void set(CtBlock replace) {
            this.element.setBody(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtTryCatchersReplaceListener implements ReplaceListListener<List> {
        private final CtTry element;

        CtTryCatchersReplaceListener(CtTry element) {
            this.element = element;
        }

        @Override
        public void set(List replace) {
            this.element.setCatchers(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtTryFinalizerReplaceListener implements ReplaceListener<CtBlock> {
        private final CtTry element;

        CtTryFinalizerReplaceListener(CtTry element) {
            this.element = element;
        }

        @Override
        public void set(CtBlock replace) {
            this.element.setFinalizer(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtTryWithResourceResourcesReplaceListener implements ReplaceListListener<List> {
        private final CtTryWithResource element;

        CtTryWithResourceResourcesReplaceListener(CtTryWithResource element) {
            this.element = element;
        }

        @Override
        public void set(List replace) {
            this.element.setResources(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtTypeParameterReferenceBoundingTypeReplaceListener implements ReplaceListener<CtTypeReference> {
        private final CtTypeParameterReference element;

        CtTypeParameterReferenceBoundingTypeReplaceListener(CtTypeParameterReference element) {
            this.element = element;
        }

        @Override
        public void set(CtTypeReference replace) {
            this.element.setBoundingType(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtIntersectionTypeReferenceBoundsReplaceListener implements ReplaceListListener<List> {
        private final CtIntersectionTypeReference element;

        CtIntersectionTypeReferenceBoundsReplaceListener(CtIntersectionTypeReference element) {
            this.element = element;
        }

        @Override
        public void set(List replace) {
            this.element.setBounds(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtTypeAccessAccessedTypeReplaceListener implements ReplaceListener<CtTypeReference> {
        private final CtTypeAccess element;

        CtTypeAccessAccessedTypeReplaceListener(CtTypeAccess element) {
            this.element = element;
        }

        @Override
        public void set(CtTypeReference replace) {
            this.element.setAccessedType(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtUnaryOperatorOperandReplaceListener implements ReplaceListener<CtExpression> {
        private final CtUnaryOperator element;

        CtUnaryOperatorOperandReplaceListener(CtUnaryOperator element) {
            this.element = element;
        }

        @Override
        public void set(CtExpression replace) {
            this.element.setOperand(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtVariableAccessVariableReplaceListener implements ReplaceListener<CtVariableReference> {
        private final CtVariableAccess element;

        CtVariableAccessVariableReplaceListener(CtVariableAccess element) {
            this.element = element;
        }

        @Override
        public void set(CtVariableReference replace) {
            this.element.setVariable(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtWhileLoopingExpressionReplaceListener implements ReplaceListener<CtExpression> {
        private final CtWhile element;

        CtWhileLoopingExpressionReplaceListener(CtWhile element) {
            this.element = element;
        }

        @Override
        public void set(CtExpression replace) {
            this.element.setLoopingExpression(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtFieldAccessVariableReplaceListener implements ReplaceListener<CtFieldReference> {
        private final CtVariableAccess element;

        CtFieldAccessVariableReplaceListener(CtVariableAccess element) {
            this.element = element;
        }

        @Override
        public void set(CtFieldReference replace) {
            this.element.setVariable(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtJavaDocTagsReplaceListener implements ReplaceListListener<List> {
        private final CtJavaDoc element;

        CtJavaDocTagsReplaceListener(CtJavaDoc element) {
            this.element = element;
        }

        @Override
        public void set(List replace) {
            this.element.setTags(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtImportReferenceReplaceListener implements ReplaceListener<CtReference> {
        private final CtImport element;

        CtImportReferenceReplaceListener(CtImport element) {
            this.element = element;
        }

        @Override
        public void set(CtReference replace) {
            this.element.setReference(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtModuleModuleDirectivesReplaceListener implements ReplaceListListener<List> {
        private final CtModule element;

        CtModuleModuleDirectivesReplaceListener(CtModule element) {
            this.element = element;
        }

        @Override
        public void set(List replace) {
            this.element.setModuleDirectives(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtModuleRootPackageReplaceListener implements ReplaceListener<CtPackage> {
        private final CtModule element;

        CtModuleRootPackageReplaceListener(CtModule element) {
            this.element = element;
        }

        @Override
        public void set(CtPackage replace) {
            this.element.setRootPackage(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtPackageExportPackageReferenceReplaceListener implements ReplaceListener<CtPackageReference> {
        private final CtPackageExport element;

        CtPackageExportPackageReferenceReplaceListener(CtPackageExport element) {
            this.element = element;
        }

        @Override
        public void set(CtPackageReference replace) {
            this.element.setPackageReference(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtPackageExportTargetExportReplaceListener implements ReplaceListListener<List> {
        private final CtPackageExport element;

        CtPackageExportTargetExportReplaceListener(CtPackageExport element) {
            this.element = element;
        }

        @Override
        public void set(List replace) {
            this.element.setTargetExport(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtModuleRequirementModuleReferenceReplaceListener implements ReplaceListener<CtModuleReference> {
        private final CtModuleRequirement element;

        CtModuleRequirementModuleReferenceReplaceListener(CtModuleRequirement element) {
            this.element = element;
        }

        @Override
        public void set(CtModuleReference replace) {
            this.element.setModuleReference(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtProvidedServiceServiceTypeReplaceListener implements ReplaceListener<CtTypeReference> {
        private final CtProvidedService element;

        CtProvidedServiceServiceTypeReplaceListener(CtProvidedService element) {
            this.element = element;
        }

        @Override
        public void set(CtTypeReference replace) {
            this.element.setServiceType(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtProvidedServiceImplementationTypesReplaceListener implements ReplaceListListener<List> {
        private final CtProvidedService element;

        CtProvidedServiceImplementationTypesReplaceListener(CtProvidedService element) {
            this.element = element;
        }

        @Override
        public void set(List replace) {
            this.element.setImplementationTypes(replace);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    class CtUsedServiceServiceTypeReplaceListener implements ReplaceListener<CtTypeReference> {
        private final CtUsedService element;

        CtUsedServiceServiceTypeReplaceListener(CtUsedService element) {
            this.element = element;
        }

        @Override
        public void set(CtTypeReference replace) {
            this.element.setServiceType(replace);
        }
    }

    public static void replace(CtElement original, CtElement replace) {
        try {
            new ReplacementVisitor(original, (replace == null ? ReplacementVisitor.EMPTY : new CtElement[]{ replace })).scan(original.getParent());
        } catch (InvalidReplaceException e) {
            throw e;
        } catch (SpoonException ignore) {
        }
    }

    public static <E extends CtElement> void replace(CtElement original, Collection<E> replaces) {
        try {
            new ReplacementVisitor(original, replaces.toArray(new CtElement[replaces.size()])).scan(original.getParent());
        } catch (InvalidReplaceException e) {
            throw e;
        } catch (SpoonException ignore) {
        }
    }

    private CtElement original;

    private CtElement[] replace;

    private static final CtElement[] EMPTY = new CtElement[0];

    private ReplacementVisitor(CtElement original, CtElement... replace) {
        this.original = original;
        this.replace = (replace == null) ? ReplacementVisitor.EMPTY : replace;
    }

    private <K, V extends CtElement> void replaceInMapIfExist(Map<K, V> mapProtected, ReplaceMapListener listener) {
        Map<K, V> map = new HashMap<>(mapProtected);
        V shouldBeDeleted = null;
        K key = null;
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if ((entry.getValue()) == (original)) {
                shouldBeDeleted = entry.getValue();
                key = entry.getKey();
                break;
            }
        }
        if (shouldBeDeleted != null) {
            if ((replace.length) > 0) {
                if ((replace.length) > 1) {
                    throw new InvalidReplaceException(("Cannot replace single value by multiple values in " + (listener.getClass().getSimpleName())));
                }
                V val = ((V) (replace[0]));
                if (val != null) {
                    map.put(key, val);
                    val.setParent(shouldBeDeleted.getParent());
                }else {
                    map.remove(key);
                }
            }else {
                map.remove(key);
            }
            listener.set(map);
        }
    }

    private <T extends CtElement> void replaceInSetIfExist(Set<T> setProtected, ReplaceSetListener listener) {
        Set<T> set = new HashSet<>(setProtected);
        T shouldBeDeleted = null;
        for (T element : set) {
            if (element == (original)) {
                shouldBeDeleted = element;
                break;
            }
        }
        if (shouldBeDeleted != null) {
            set.remove(shouldBeDeleted);
            for (CtElement ele : replace) {
                if (ele != null) {
                    set.add(((T) (ele)));
                    ele.setParent(shouldBeDeleted.getParent());
                }
            }
            listener.set(set);
        }
    }

    private <T extends CtElement> void replaceInListIfExist(List<T> listProtected, ReplaceListListener listener) {
        List<T> list = new ArrayList<>(listProtected);
        T shouldBeDeleted = null;
        int index = 0;
        for (int i = 0; i < (list.size()); i++) {
            if ((list.get(i)) == (original)) {
                index = i;
                shouldBeDeleted = list.get(i);
                break;
            }
        }
        if (shouldBeDeleted != null) {
            list.remove(index);
            if ((replace.length) > 0) {
                for (int i = 0; i < (replace.length); i++) {
                    T ele = ((T) (replace[i]));
                    if (ele != null) {
                        list.add(index, ele);
                        ele.setParent(shouldBeDeleted.getParent());
                        index = index + 1;
                    }
                }
            }
            listener.set(list);
        }
    }

    private void replaceElementIfExist(CtElement candidate, ReplaceListener listener) {
        if (candidate == (original)) {
            CtElement val = null;
            if ((replace.length) > 0) {
                if ((replace.length) > 1) {
                    throw new InvalidReplaceException(("Cannot replace single value by multiple values in " + (listener.getClass().getSimpleName())));
                }
                val = replace[0];
            }
            if (val != null) {
                val.setParent(candidate.getParent());
            }
            listener.set(val);
        }
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <A extends Annotation> void visitCtAnnotation(final CtAnnotation<A> annotation) {
        replaceElementIfExist(annotation.getType(), new ReplacementVisitor.CtTypedElementTypeReplaceListener(annotation));
        replaceInListIfExist(annotation.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(annotation));
        replaceElementIfExist(annotation.getAnnotationType(), new ReplacementVisitor.CtAnnotationAnnotationTypeReplaceListener(annotation));
        replaceInListIfExist(annotation.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(annotation));
        replaceInMapIfExist(annotation.getValues(), new ReplacementVisitor.CtAnnotationValuesReplaceListener(annotation));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <A extends Annotation> void visitCtAnnotationType(final CtAnnotationType<A> annotationType) {
        replaceInListIfExist(annotationType.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(annotationType));
        replaceInListIfExist(annotationType.getTypeMembers(), new ReplacementVisitor.CtTypeTypeMembersReplaceListener(annotationType));
        replaceInListIfExist(annotationType.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(annotationType));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public void visitCtAnonymousExecutable(final CtAnonymousExecutable anonymousExec) {
        replaceInListIfExist(anonymousExec.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(anonymousExec));
        replaceElementIfExist(anonymousExec.getBody(), new ReplacementVisitor.CtExecutableBodyReplaceListener(anonymousExec));
        replaceInListIfExist(anonymousExec.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(anonymousExec));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtArrayRead(final CtArrayRead<T> arrayRead) {
        replaceInListIfExist(arrayRead.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(arrayRead));
        replaceElementIfExist(arrayRead.getType(), new ReplacementVisitor.CtTypedElementTypeReplaceListener(arrayRead));
        replaceInListIfExist(arrayRead.getTypeCasts(), new ReplacementVisitor.CtExpressionTypeCastsReplaceListener(arrayRead));
        replaceElementIfExist(arrayRead.getTarget(), new ReplacementVisitor.CtTargetedExpressionTargetReplaceListener(arrayRead));
        replaceElementIfExist(arrayRead.getIndexExpression(), new ReplacementVisitor.CtArrayAccessIndexExpressionReplaceListener(arrayRead));
        replaceInListIfExist(arrayRead.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(arrayRead));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtArrayWrite(final CtArrayWrite<T> arrayWrite) {
        replaceInListIfExist(arrayWrite.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(arrayWrite));
        replaceElementIfExist(arrayWrite.getType(), new ReplacementVisitor.CtTypedElementTypeReplaceListener(arrayWrite));
        replaceInListIfExist(arrayWrite.getTypeCasts(), new ReplacementVisitor.CtExpressionTypeCastsReplaceListener(arrayWrite));
        replaceElementIfExist(arrayWrite.getTarget(), new ReplacementVisitor.CtTargetedExpressionTargetReplaceListener(arrayWrite));
        replaceElementIfExist(arrayWrite.getIndexExpression(), new ReplacementVisitor.CtArrayAccessIndexExpressionReplaceListener(arrayWrite));
        replaceInListIfExist(arrayWrite.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(arrayWrite));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtArrayTypeReference(final CtArrayTypeReference<T> reference) {
        replaceElementIfExist(reference.getPackage(), new ReplacementVisitor.CtTypeReferencePackageReplaceListener(reference));
        replaceElementIfExist(reference.getDeclaringType(), new ReplacementVisitor.CtTypeReferenceDeclaringTypeReplaceListener(reference));
        replaceElementIfExist(reference.getComponentType(), new ReplacementVisitor.CtArrayTypeReferenceComponentTypeReplaceListener(reference));
        replaceInListIfExist(reference.getActualTypeArguments(), new ReplacementVisitor.CtActualTypeContainerActualTypeArgumentsReplaceListener(reference));
        replaceInListIfExist(reference.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(reference));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtAssert(final CtAssert<T> asserted) {
        replaceInListIfExist(asserted.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(asserted));
        replaceElementIfExist(asserted.getAssertExpression(), new ReplacementVisitor.CtAssertAssertExpressionReplaceListener(asserted));
        replaceElementIfExist(asserted.getExpression(), new ReplacementVisitor.CtAssertExpressionReplaceListener(asserted));
        replaceInListIfExist(asserted.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(asserted));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T, A extends T> void visitCtAssignment(final CtAssignment<T, A> assignement) {
        replaceInListIfExist(assignement.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(assignement));
        replaceElementIfExist(assignement.getType(), new ReplacementVisitor.CtTypedElementTypeReplaceListener(assignement));
        replaceInListIfExist(assignement.getTypeCasts(), new ReplacementVisitor.CtExpressionTypeCastsReplaceListener(assignement));
        replaceElementIfExist(assignement.getAssigned(), new ReplacementVisitor.CtAssignmentAssignedReplaceListener(assignement));
        replaceElementIfExist(assignement.getAssignment(), new ReplacementVisitor.CtRHSReceiverAssignmentReplaceListener(assignement));
        replaceInListIfExist(assignement.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(assignement));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtBinaryOperator(final CtBinaryOperator<T> operator) {
        replaceInListIfExist(operator.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(operator));
        replaceElementIfExist(operator.getType(), new ReplacementVisitor.CtTypedElementTypeReplaceListener(operator));
        replaceInListIfExist(operator.getTypeCasts(), new ReplacementVisitor.CtExpressionTypeCastsReplaceListener(operator));
        replaceElementIfExist(operator.getLeftHandOperand(), new ReplacementVisitor.CtBinaryOperatorLeftHandOperandReplaceListener(operator));
        replaceElementIfExist(operator.getRightHandOperand(), new ReplacementVisitor.CtBinaryOperatorRightHandOperandReplaceListener(operator));
        replaceInListIfExist(operator.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(operator));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <R> void visitCtBlock(final CtBlock<R> block) {
        replaceInListIfExist(block.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(block));
        replaceInListIfExist(block.getStatements(), new ReplacementVisitor.CtStatementListStatementsReplaceListener(block));
        replaceInListIfExist(block.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(block));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public void visitCtBreak(final CtBreak breakStatement) {
        replaceInListIfExist(breakStatement.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(breakStatement));
        replaceInListIfExist(breakStatement.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(breakStatement));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <S> void visitCtCase(final CtCase<S> caseStatement) {
        replaceInListIfExist(caseStatement.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(caseStatement));
        replaceElementIfExist(caseStatement.getCaseExpression(), new ReplacementVisitor.CtCaseCaseExpressionReplaceListener(caseStatement));
        replaceInListIfExist(caseStatement.getStatements(), new ReplacementVisitor.CtStatementListStatementsReplaceListener(caseStatement));
        replaceInListIfExist(caseStatement.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(caseStatement));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public void visitCtCatch(final CtCatch catchBlock) {
        replaceInListIfExist(catchBlock.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(catchBlock));
        replaceElementIfExist(catchBlock.getParameter(), new ReplacementVisitor.CtCatchParameterReplaceListener(catchBlock));
        replaceElementIfExist(catchBlock.getBody(), new ReplacementVisitor.CtCatchBodyReplaceListener(catchBlock));
        replaceInListIfExist(catchBlock.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(catchBlock));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtClass(final CtClass<T> ctClass) {
        replaceInListIfExist(ctClass.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(ctClass));
        replaceElementIfExist(ctClass.getSuperclass(), new ReplacementVisitor.CtTypeInformationSuperclassReplaceListener(ctClass));
        replaceInSetIfExist(ctClass.getSuperInterfaces(), new ReplacementVisitor.CtTypeInformationSuperInterfacesReplaceListener(ctClass));
        replaceInListIfExist(ctClass.getFormalCtTypeParameters(), new ReplacementVisitor.CtFormalTypeDeclarerFormalCtTypeParametersReplaceListener(ctClass));
        replaceInListIfExist(ctClass.getTypeMembers(), new ReplacementVisitor.CtTypeTypeMembersReplaceListener(ctClass));
        replaceInListIfExist(ctClass.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(ctClass));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public void visitCtTypeParameter(CtTypeParameter typeParameter) {
        replaceInListIfExist(typeParameter.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(typeParameter));
        replaceElementIfExist(typeParameter.getSuperclass(), new ReplacementVisitor.CtTypeInformationSuperclassReplaceListener(typeParameter));
        replaceInListIfExist(typeParameter.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(typeParameter));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtConditional(final CtConditional<T> conditional) {
        replaceElementIfExist(conditional.getType(), new ReplacementVisitor.CtTypedElementTypeReplaceListener(conditional));
        replaceInListIfExist(conditional.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(conditional));
        replaceElementIfExist(conditional.getCondition(), new ReplacementVisitor.CtConditionalConditionReplaceListener(conditional));
        replaceElementIfExist(conditional.getThenExpression(), new ReplacementVisitor.CtConditionalThenExpressionReplaceListener(conditional));
        replaceElementIfExist(conditional.getElseExpression(), new ReplacementVisitor.CtConditionalElseExpressionReplaceListener(conditional));
        replaceInListIfExist(conditional.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(conditional));
        replaceInListIfExist(conditional.getTypeCasts(), new ReplacementVisitor.CtExpressionTypeCastsReplaceListener(conditional));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtConstructor(final CtConstructor<T> c) {
        replaceInListIfExist(c.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(c));
        replaceInListIfExist(c.getParameters(), new ReplacementVisitor.CtExecutableParametersReplaceListener(c));
        replaceInSetIfExist(c.getThrownTypes(), new ReplacementVisitor.CtExecutableThrownTypesReplaceListener(c));
        replaceInListIfExist(c.getFormalCtTypeParameters(), new ReplacementVisitor.CtFormalTypeDeclarerFormalCtTypeParametersReplaceListener(c));
        replaceElementIfExist(c.getBody(), new ReplacementVisitor.CtExecutableBodyReplaceListener(c));
        replaceInListIfExist(c.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(c));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public void visitCtContinue(final CtContinue continueStatement) {
        replaceInListIfExist(continueStatement.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(continueStatement));
        replaceInListIfExist(continueStatement.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(continueStatement));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public void visitCtDo(final CtDo doLoop) {
        replaceInListIfExist(doLoop.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(doLoop));
        replaceElementIfExist(doLoop.getLoopingExpression(), new ReplacementVisitor.CtDoLoopingExpressionReplaceListener(doLoop));
        replaceElementIfExist(doLoop.getBody(), new ReplacementVisitor.CtLoopBodyReplaceListener(doLoop));
        replaceInListIfExist(doLoop.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(doLoop));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T extends Enum<?>> void visitCtEnum(final CtEnum<T> ctEnum) {
        replaceInListIfExist(ctEnum.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(ctEnum));
        replaceInSetIfExist(ctEnum.getSuperInterfaces(), new ReplacementVisitor.CtTypeInformationSuperInterfacesReplaceListener(ctEnum));
        replaceInListIfExist(ctEnum.getTypeMembers(), new ReplacementVisitor.CtTypeTypeMembersReplaceListener(ctEnum));
        replaceInListIfExist(ctEnum.getEnumValues(), new ReplacementVisitor.CtEnumEnumValuesReplaceListener(ctEnum));
        replaceInListIfExist(ctEnum.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(ctEnum));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtExecutableReference(final CtExecutableReference<T> reference) {
        replaceElementIfExist(reference.getDeclaringType(), new ReplacementVisitor.CtExecutableReferenceDeclaringTypeReplaceListener(reference));
        replaceElementIfExist(reference.getType(), new ReplacementVisitor.CtExecutableReferenceTypeReplaceListener(reference));
        replaceInListIfExist(reference.getParameters(), new ReplacementVisitor.CtExecutableReferenceParametersReplaceListener(reference));
        replaceInListIfExist(reference.getActualTypeArguments(), new ReplacementVisitor.CtActualTypeContainerActualTypeArgumentsReplaceListener(reference));
        replaceInListIfExist(reference.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(reference));
        replaceInListIfExist(reference.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(reference));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtField(final CtField<T> f) {
        replaceInListIfExist(f.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(f));
        replaceElementIfExist(f.getType(), new ReplacementVisitor.CtTypedElementTypeReplaceListener(f));
        replaceElementIfExist(f.getDefaultExpression(), new ReplacementVisitor.CtVariableDefaultExpressionReplaceListener(f));
        replaceInListIfExist(f.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(f));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtEnumValue(final CtEnumValue<T> enumValue) {
        replaceInListIfExist(enumValue.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(enumValue));
        replaceElementIfExist(enumValue.getType(), new ReplacementVisitor.CtTypedElementTypeReplaceListener(enumValue));
        replaceElementIfExist(enumValue.getDefaultExpression(), new ReplacementVisitor.CtVariableDefaultExpressionReplaceListener(enumValue));
        replaceInListIfExist(enumValue.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(enumValue));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtThisAccess(final CtThisAccess<T> thisAccess) {
        replaceInListIfExist(thisAccess.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(thisAccess));
        replaceInListIfExist(thisAccess.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(thisAccess));
        replaceElementIfExist(thisAccess.getType(), new ReplacementVisitor.CtTypedElementTypeReplaceListener(thisAccess));
        replaceInListIfExist(thisAccess.getTypeCasts(), new ReplacementVisitor.CtExpressionTypeCastsReplaceListener(thisAccess));
        replaceElementIfExist(thisAccess.getTarget(), new ReplacementVisitor.CtTargetedExpressionTargetReplaceListener(thisAccess));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtAnnotationFieldAccess(final CtAnnotationFieldAccess<T> annotationFieldAccess) {
        replaceInListIfExist(annotationFieldAccess.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(annotationFieldAccess));
        replaceInListIfExist(annotationFieldAccess.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(annotationFieldAccess));
        replaceInListIfExist(annotationFieldAccess.getTypeCasts(), new ReplacementVisitor.CtExpressionTypeCastsReplaceListener(annotationFieldAccess));
        replaceElementIfExist(annotationFieldAccess.getTarget(), new ReplacementVisitor.CtTargetedExpressionTargetReplaceListener(annotationFieldAccess));
        replaceElementIfExist(annotationFieldAccess.getVariable(), new ReplacementVisitor.CtAnnotationFieldAccessVariableReplaceListener(annotationFieldAccess));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtFieldReference(final CtFieldReference<T> reference) {
        replaceElementIfExist(reference.getDeclaringType(), new ReplacementVisitor.CtFieldReferenceDeclaringTypeReplaceListener(reference));
        replaceElementIfExist(reference.getType(), new ReplacementVisitor.CtVariableReferenceTypeReplaceListener(reference));
        replaceInListIfExist(reference.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(reference));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public void visitCtFor(final CtFor forLoop) {
        replaceInListIfExist(forLoop.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(forLoop));
        replaceInListIfExist(forLoop.getForInit(), new ReplacementVisitor.CtForForInitReplaceListener(forLoop));
        replaceElementIfExist(forLoop.getExpression(), new ReplacementVisitor.CtForExpressionReplaceListener(forLoop));
        replaceInListIfExist(forLoop.getForUpdate(), new ReplacementVisitor.CtForForUpdateReplaceListener(forLoop));
        replaceElementIfExist(forLoop.getBody(), new ReplacementVisitor.CtLoopBodyReplaceListener(forLoop));
        replaceInListIfExist(forLoop.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(forLoop));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public void visitCtForEach(final CtForEach foreach) {
        replaceInListIfExist(foreach.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(foreach));
        replaceElementIfExist(foreach.getVariable(), new ReplacementVisitor.CtForEachVariableReplaceListener(foreach));
        replaceElementIfExist(foreach.getExpression(), new ReplacementVisitor.CtForEachExpressionReplaceListener(foreach));
        replaceElementIfExist(foreach.getBody(), new ReplacementVisitor.CtLoopBodyReplaceListener(foreach));
        replaceInListIfExist(foreach.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(foreach));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public void visitCtIf(final CtIf ifElement) {
        replaceInListIfExist(ifElement.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(ifElement));
        replaceElementIfExist(ifElement.getCondition(), new ReplacementVisitor.CtIfConditionReplaceListener(ifElement));
        replaceElementIfExist(((CtStatement) (ifElement.getThenStatement())), new ReplacementVisitor.CtIfThenStatementReplaceListener(ifElement));
        replaceElementIfExist(((CtStatement) (ifElement.getElseStatement())), new ReplacementVisitor.CtIfElseStatementReplaceListener(ifElement));
        replaceInListIfExist(ifElement.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(ifElement));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtInterface(final CtInterface<T> intrface) {
        replaceInListIfExist(intrface.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(intrface));
        replaceInSetIfExist(intrface.getSuperInterfaces(), new ReplacementVisitor.CtTypeInformationSuperInterfacesReplaceListener(intrface));
        replaceInListIfExist(intrface.getFormalCtTypeParameters(), new ReplacementVisitor.CtFormalTypeDeclarerFormalCtTypeParametersReplaceListener(intrface));
        replaceInListIfExist(intrface.getTypeMembers(), new ReplacementVisitor.CtTypeTypeMembersReplaceListener(intrface));
        replaceInListIfExist(intrface.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(intrface));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtInvocation(final CtInvocation<T> invocation) {
        replaceInListIfExist(invocation.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(invocation));
        replaceInListIfExist(invocation.getTypeCasts(), new ReplacementVisitor.CtExpressionTypeCastsReplaceListener(invocation));
        replaceElementIfExist(invocation.getTarget(), new ReplacementVisitor.CtTargetedExpressionTargetReplaceListener(invocation));
        replaceElementIfExist(invocation.getExecutable(), new ReplacementVisitor.CtAbstractInvocationExecutableReplaceListener(invocation));
        replaceInListIfExist(invocation.getArguments(), new ReplacementVisitor.CtAbstractInvocationArgumentsReplaceListener(invocation));
        replaceInListIfExist(invocation.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(invocation));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtLiteral(final CtLiteral<T> literal) {
        replaceInListIfExist(literal.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(literal));
        replaceElementIfExist(literal.getType(), new ReplacementVisitor.CtTypedElementTypeReplaceListener(literal));
        replaceInListIfExist(literal.getTypeCasts(), new ReplacementVisitor.CtExpressionTypeCastsReplaceListener(literal));
        replaceInListIfExist(literal.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(literal));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtLocalVariable(final CtLocalVariable<T> localVariable) {
        replaceInListIfExist(localVariable.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(localVariable));
        replaceElementIfExist(localVariable.getType(), new ReplacementVisitor.CtTypedElementTypeReplaceListener(localVariable));
        replaceElementIfExist(localVariable.getDefaultExpression(), new ReplacementVisitor.CtVariableDefaultExpressionReplaceListener(localVariable));
        replaceInListIfExist(localVariable.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(localVariable));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtLocalVariableReference(final CtLocalVariableReference<T> reference) {
        replaceElementIfExist(reference.getType(), new ReplacementVisitor.CtVariableReferenceTypeReplaceListener(reference));
        replaceInListIfExist(reference.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(reference));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtCatchVariable(final CtCatchVariable<T> catchVariable) {
        replaceInListIfExist(catchVariable.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(catchVariable));
        replaceInListIfExist(catchVariable.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(catchVariable));
        replaceInListIfExist(catchVariable.getMultiTypes(), new ReplacementVisitor.CtMultiTypedElementMultiTypesReplaceListener(catchVariable));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtCatchVariableReference(final CtCatchVariableReference<T> reference) {
        replaceElementIfExist(reference.getType(), new ReplacementVisitor.CtVariableReferenceTypeReplaceListener(reference));
        replaceInListIfExist(reference.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(reference));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtMethod(final CtMethod<T> m) {
        replaceInListIfExist(m.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(m));
        replaceInListIfExist(m.getFormalCtTypeParameters(), new ReplacementVisitor.CtFormalTypeDeclarerFormalCtTypeParametersReplaceListener(m));
        replaceElementIfExist(m.getType(), new ReplacementVisitor.CtTypedElementTypeReplaceListener(m));
        replaceInListIfExist(m.getParameters(), new ReplacementVisitor.CtExecutableParametersReplaceListener(m));
        replaceInSetIfExist(m.getThrownTypes(), new ReplacementVisitor.CtExecutableThrownTypesReplaceListener(m));
        replaceElementIfExist(m.getBody(), new ReplacementVisitor.CtExecutableBodyReplaceListener(m));
        replaceInListIfExist(m.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(m));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtAnnotationMethod(CtAnnotationMethod<T> annotationMethod) {
        replaceInListIfExist(annotationMethod.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(annotationMethod));
        replaceElementIfExist(annotationMethod.getType(), new ReplacementVisitor.CtTypedElementTypeReplaceListener(annotationMethod));
        replaceElementIfExist(annotationMethod.getDefaultExpression(), new ReplacementVisitor.CtAnnotationMethodDefaultExpressionReplaceListener(annotationMethod));
        replaceInListIfExist(annotationMethod.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(annotationMethod));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtNewArray(final CtNewArray<T> newArray) {
        replaceInListIfExist(newArray.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(newArray));
        replaceElementIfExist(newArray.getType(), new ReplacementVisitor.CtTypedElementTypeReplaceListener(newArray));
        replaceInListIfExist(newArray.getTypeCasts(), new ReplacementVisitor.CtExpressionTypeCastsReplaceListener(newArray));
        replaceInListIfExist(newArray.getElements(), new ReplacementVisitor.CtNewArrayElementsReplaceListener(newArray));
        replaceInListIfExist(newArray.getDimensionExpressions(), new ReplacementVisitor.CtNewArrayDimensionExpressionsReplaceListener(newArray));
        replaceInListIfExist(newArray.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(newArray));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtConstructorCall(final CtConstructorCall<T> ctConstructorCall) {
        replaceInListIfExist(ctConstructorCall.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(ctConstructorCall));
        replaceInListIfExist(ctConstructorCall.getTypeCasts(), new ReplacementVisitor.CtExpressionTypeCastsReplaceListener(ctConstructorCall));
        replaceElementIfExist(ctConstructorCall.getExecutable(), new ReplacementVisitor.CtAbstractInvocationExecutableReplaceListener(ctConstructorCall));
        replaceElementIfExist(ctConstructorCall.getTarget(), new ReplacementVisitor.CtTargetedExpressionTargetReplaceListener(ctConstructorCall));
        replaceInListIfExist(ctConstructorCall.getArguments(), new ReplacementVisitor.CtAbstractInvocationArgumentsReplaceListener(ctConstructorCall));
        replaceInListIfExist(ctConstructorCall.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(ctConstructorCall));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtNewClass(final CtNewClass<T> newClass) {
        replaceInListIfExist(newClass.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(newClass));
        replaceInListIfExist(newClass.getTypeCasts(), new ReplacementVisitor.CtExpressionTypeCastsReplaceListener(newClass));
        replaceElementIfExist(newClass.getExecutable(), new ReplacementVisitor.CtAbstractInvocationExecutableReplaceListener(newClass));
        replaceElementIfExist(newClass.getTarget(), new ReplacementVisitor.CtTargetedExpressionTargetReplaceListener(newClass));
        replaceInListIfExist(newClass.getArguments(), new ReplacementVisitor.CtAbstractInvocationArgumentsReplaceListener(newClass));
        replaceElementIfExist(newClass.getAnonymousClass(), new ReplacementVisitor.CtNewClassAnonymousClassReplaceListener(newClass));
        replaceInListIfExist(newClass.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(newClass));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtLambda(final CtLambda<T> lambda) {
        replaceInListIfExist(lambda.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(lambda));
        replaceElementIfExist(lambda.getType(), new ReplacementVisitor.CtTypedElementTypeReplaceListener(lambda));
        replaceInListIfExist(lambda.getTypeCasts(), new ReplacementVisitor.CtExpressionTypeCastsReplaceListener(lambda));
        replaceInListIfExist(lambda.getParameters(), new ReplacementVisitor.CtExecutableParametersReplaceListener(lambda));
        replaceElementIfExist(lambda.getBody(), new ReplacementVisitor.CtExecutableBodyReplaceListener(lambda));
        replaceElementIfExist(lambda.getExpression(), new ReplacementVisitor.CtLambdaExpressionReplaceListener(lambda));
        replaceInListIfExist(lambda.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(lambda));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T, E extends CtExpression<?>> void visitCtExecutableReferenceExpression(final CtExecutableReferenceExpression<T, E> expression) {
        replaceInListIfExist(expression.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(expression));
        replaceInListIfExist(expression.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(expression));
        replaceElementIfExist(expression.getType(), new ReplacementVisitor.CtTypedElementTypeReplaceListener(expression));
        replaceInListIfExist(expression.getTypeCasts(), new ReplacementVisitor.CtExpressionTypeCastsReplaceListener(expression));
        replaceElementIfExist(expression.getExecutable(), new ReplacementVisitor.CtExecutableReferenceExpressionExecutableReplaceListener(expression));
        replaceElementIfExist(expression.getTarget(), new ReplacementVisitor.CtTargetedExpressionTargetReplaceListener(expression));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T, A extends T> void visitCtOperatorAssignment(final CtOperatorAssignment<T, A> assignment) {
        replaceInListIfExist(assignment.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(assignment));
        replaceElementIfExist(assignment.getType(), new ReplacementVisitor.CtTypedElementTypeReplaceListener(assignment));
        replaceInListIfExist(assignment.getTypeCasts(), new ReplacementVisitor.CtExpressionTypeCastsReplaceListener(assignment));
        replaceElementIfExist(assignment.getAssigned(), new ReplacementVisitor.CtAssignmentAssignedReplaceListener(assignment));
        replaceElementIfExist(assignment.getAssignment(), new ReplacementVisitor.CtRHSReceiverAssignmentReplaceListener(assignment));
        replaceInListIfExist(assignment.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(assignment));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public void visitCtPackage(final CtPackage ctPackage) {
        replaceInListIfExist(ctPackage.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(ctPackage));
        replaceInSetIfExist(ctPackage.getPackages(), new ReplacementVisitor.CtPackagePackagesReplaceListener(ctPackage));
        replaceInSetIfExist(ctPackage.getTypes(), new ReplacementVisitor.CtPackageTypesReplaceListener(ctPackage));
        replaceInListIfExist(ctPackage.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(ctPackage));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public void visitCtPackageReference(final CtPackageReference reference) {
        replaceInListIfExist(reference.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(reference));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtParameter(final CtParameter<T> parameter) {
        replaceInListIfExist(parameter.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(parameter));
        replaceElementIfExist(parameter.getType(), new ReplacementVisitor.CtTypedElementTypeReplaceListener(parameter));
        replaceInListIfExist(parameter.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(parameter));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtParameterReference(final CtParameterReference<T> reference) {
        replaceElementIfExist(reference.getType(), new ReplacementVisitor.CtVariableReferenceTypeReplaceListener(reference));
        replaceInListIfExist(reference.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(reference));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <R> void visitCtReturn(final CtReturn<R> returnStatement) {
        replaceInListIfExist(returnStatement.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(returnStatement));
        replaceElementIfExist(returnStatement.getReturnedExpression(), new ReplacementVisitor.CtReturnReturnedExpressionReplaceListener(returnStatement));
        replaceInListIfExist(returnStatement.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(returnStatement));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <R> void visitCtStatementList(final CtStatementList statements) {
        replaceInListIfExist(statements.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(statements));
        replaceInListIfExist(statements.getStatements(), new ReplacementVisitor.CtStatementListStatementsReplaceListener(statements));
        replaceInListIfExist(statements.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(statements));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <S> void visitCtSwitch(final CtSwitch<S> switchStatement) {
        replaceInListIfExist(switchStatement.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(switchStatement));
        replaceElementIfExist(switchStatement.getSelector(), new ReplacementVisitor.CtSwitchSelectorReplaceListener(switchStatement));
        replaceInListIfExist(switchStatement.getCases(), new ReplacementVisitor.CtSwitchCasesReplaceListener(switchStatement));
        replaceInListIfExist(switchStatement.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(switchStatement));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public void visitCtSynchronized(final CtSynchronized synchro) {
        replaceInListIfExist(synchro.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(synchro));
        replaceElementIfExist(synchro.getExpression(), new ReplacementVisitor.CtSynchronizedExpressionReplaceListener(synchro));
        replaceElementIfExist(synchro.getBlock(), new ReplacementVisitor.CtSynchronizedBlockReplaceListener(synchro));
        replaceInListIfExist(synchro.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(synchro));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public void visitCtThrow(final CtThrow throwStatement) {
        replaceInListIfExist(throwStatement.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(throwStatement));
        replaceElementIfExist(throwStatement.getThrownExpression(), new ReplacementVisitor.CtThrowThrownExpressionReplaceListener(throwStatement));
        replaceInListIfExist(throwStatement.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(throwStatement));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public void visitCtTry(final CtTry tryBlock) {
        replaceInListIfExist(tryBlock.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(tryBlock));
        replaceElementIfExist(tryBlock.getBody(), new ReplacementVisitor.CtTryBodyReplaceListener(tryBlock));
        replaceInListIfExist(tryBlock.getCatchers(), new ReplacementVisitor.CtTryCatchersReplaceListener(tryBlock));
        replaceElementIfExist(tryBlock.getFinalizer(), new ReplacementVisitor.CtTryFinalizerReplaceListener(tryBlock));
        replaceInListIfExist(tryBlock.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(tryBlock));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public void visitCtTryWithResource(final CtTryWithResource tryWithResource) {
        replaceInListIfExist(tryWithResource.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(tryWithResource));
        replaceInListIfExist(tryWithResource.getResources(), new ReplacementVisitor.CtTryWithResourceResourcesReplaceListener(tryWithResource));
        replaceElementIfExist(tryWithResource.getBody(), new ReplacementVisitor.CtTryBodyReplaceListener(tryWithResource));
        replaceInListIfExist(tryWithResource.getCatchers(), new ReplacementVisitor.CtTryCatchersReplaceListener(tryWithResource));
        replaceElementIfExist(tryWithResource.getFinalizer(), new ReplacementVisitor.CtTryFinalizerReplaceListener(tryWithResource));
        replaceInListIfExist(tryWithResource.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(tryWithResource));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public void visitCtTypeParameterReference(final CtTypeParameterReference ref) {
        replaceElementIfExist(ref.getPackage(), new ReplacementVisitor.CtTypeReferencePackageReplaceListener(ref));
        replaceElementIfExist(ref.getDeclaringType(), new ReplacementVisitor.CtTypeReferenceDeclaringTypeReplaceListener(ref));
        replaceInListIfExist(ref.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(ref));
        replaceElementIfExist(ref.getBoundingType(), new ReplacementVisitor.CtTypeParameterReferenceBoundingTypeReplaceListener(ref));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public void visitCtWildcardReference(CtWildcardReference wildcardReference) {
        replaceElementIfExist(wildcardReference.getPackage(), new ReplacementVisitor.CtTypeReferencePackageReplaceListener(wildcardReference));
        replaceElementIfExist(wildcardReference.getDeclaringType(), new ReplacementVisitor.CtTypeReferenceDeclaringTypeReplaceListener(wildcardReference));
        replaceInListIfExist(wildcardReference.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(wildcardReference));
        replaceElementIfExist(wildcardReference.getBoundingType(), new ReplacementVisitor.CtTypeParameterReferenceBoundingTypeReplaceListener(wildcardReference));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtIntersectionTypeReference(final CtIntersectionTypeReference<T> reference) {
        replaceElementIfExist(reference.getPackage(), new ReplacementVisitor.CtTypeReferencePackageReplaceListener(reference));
        replaceElementIfExist(reference.getDeclaringType(), new ReplacementVisitor.CtTypeReferenceDeclaringTypeReplaceListener(reference));
        replaceInListIfExist(reference.getActualTypeArguments(), new ReplacementVisitor.CtActualTypeContainerActualTypeArgumentsReplaceListener(reference));
        replaceInListIfExist(reference.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(reference));
        replaceInListIfExist(reference.getBounds(), new ReplacementVisitor.CtIntersectionTypeReferenceBoundsReplaceListener(reference));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtTypeReference(final CtTypeReference<T> reference) {
        replaceElementIfExist(reference.getPackage(), new ReplacementVisitor.CtTypeReferencePackageReplaceListener(reference));
        replaceElementIfExist(reference.getDeclaringType(), new ReplacementVisitor.CtTypeReferenceDeclaringTypeReplaceListener(reference));
        replaceInListIfExist(reference.getActualTypeArguments(), new ReplacementVisitor.CtActualTypeContainerActualTypeArgumentsReplaceListener(reference));
        replaceInListIfExist(reference.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(reference));
        replaceInListIfExist(reference.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(reference));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtTypeAccess(final CtTypeAccess<T> typeAccess) {
        replaceInListIfExist(typeAccess.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(typeAccess));
        replaceInListIfExist(typeAccess.getTypeCasts(), new ReplacementVisitor.CtExpressionTypeCastsReplaceListener(typeAccess));
        replaceElementIfExist(typeAccess.getAccessedType(), new ReplacementVisitor.CtTypeAccessAccessedTypeReplaceListener(typeAccess));
        replaceInListIfExist(typeAccess.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(typeAccess));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtUnaryOperator(final CtUnaryOperator<T> operator) {
        replaceInListIfExist(operator.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(operator));
        replaceElementIfExist(operator.getType(), new ReplacementVisitor.CtTypedElementTypeReplaceListener(operator));
        replaceInListIfExist(operator.getTypeCasts(), new ReplacementVisitor.CtExpressionTypeCastsReplaceListener(operator));
        replaceElementIfExist(operator.getOperand(), new ReplacementVisitor.CtUnaryOperatorOperandReplaceListener(operator));
        replaceInListIfExist(operator.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(operator));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtVariableRead(final CtVariableRead<T> variableRead) {
        replaceInListIfExist(variableRead.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(variableRead));
        replaceInListIfExist(variableRead.getTypeCasts(), new ReplacementVisitor.CtExpressionTypeCastsReplaceListener(variableRead));
        replaceElementIfExist(variableRead.getVariable(), new ReplacementVisitor.CtVariableAccessVariableReplaceListener(variableRead));
        replaceInListIfExist(variableRead.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(variableRead));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtVariableWrite(final CtVariableWrite<T> variableWrite) {
        replaceInListIfExist(variableWrite.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(variableWrite));
        replaceInListIfExist(variableWrite.getTypeCasts(), new ReplacementVisitor.CtExpressionTypeCastsReplaceListener(variableWrite));
        replaceElementIfExist(variableWrite.getVariable(), new ReplacementVisitor.CtVariableAccessVariableReplaceListener(variableWrite));
        replaceInListIfExist(variableWrite.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(variableWrite));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public void visitCtWhile(final CtWhile whileLoop) {
        replaceInListIfExist(whileLoop.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(whileLoop));
        replaceElementIfExist(whileLoop.getLoopingExpression(), new ReplacementVisitor.CtWhileLoopingExpressionReplaceListener(whileLoop));
        replaceElementIfExist(whileLoop.getBody(), new ReplacementVisitor.CtLoopBodyReplaceListener(whileLoop));
        replaceInListIfExist(whileLoop.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(whileLoop));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtCodeSnippetExpression(final CtCodeSnippetExpression<T> expression) {
        replaceElementIfExist(expression.getType(), new ReplacementVisitor.CtTypedElementTypeReplaceListener(expression));
        replaceInListIfExist(expression.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(expression));
        replaceInListIfExist(expression.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(expression));
        replaceInListIfExist(expression.getTypeCasts(), new ReplacementVisitor.CtExpressionTypeCastsReplaceListener(expression));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public void visitCtCodeSnippetStatement(final CtCodeSnippetStatement statement) {
        replaceInListIfExist(statement.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(statement));
        replaceInListIfExist(statement.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(statement));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtUnboundVariableReference(final CtUnboundVariableReference<T> reference) {
        replaceElementIfExist(reference.getType(), new ReplacementVisitor.CtVariableReferenceTypeReplaceListener(reference));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtFieldRead(final CtFieldRead<T> fieldRead) {
        replaceInListIfExist(fieldRead.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(fieldRead));
        replaceInListIfExist(fieldRead.getTypeCasts(), new ReplacementVisitor.CtExpressionTypeCastsReplaceListener(fieldRead));
        replaceElementIfExist(fieldRead.getTarget(), new ReplacementVisitor.CtTargetedExpressionTargetReplaceListener(fieldRead));
        replaceElementIfExist(fieldRead.getVariable(), new ReplacementVisitor.CtFieldAccessVariableReplaceListener(fieldRead));
        replaceInListIfExist(fieldRead.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(fieldRead));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtFieldWrite(final CtFieldWrite<T> fieldWrite) {
        replaceInListIfExist(fieldWrite.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(fieldWrite));
        replaceInListIfExist(fieldWrite.getTypeCasts(), new ReplacementVisitor.CtExpressionTypeCastsReplaceListener(fieldWrite));
        replaceElementIfExist(fieldWrite.getTarget(), new ReplacementVisitor.CtTargetedExpressionTargetReplaceListener(fieldWrite));
        replaceElementIfExist(fieldWrite.getVariable(), new ReplacementVisitor.CtFieldAccessVariableReplaceListener(fieldWrite));
        replaceInListIfExist(fieldWrite.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(fieldWrite));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public <T> void visitCtSuperAccess(final CtSuperAccess<T> f) {
        replaceInListIfExist(f.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(f));
        replaceInListIfExist(f.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(f));
        replaceInListIfExist(f.getTypeCasts(), new ReplacementVisitor.CtExpressionTypeCastsReplaceListener(f));
        replaceElementIfExist(f.getTarget(), new ReplacementVisitor.CtTargetedExpressionTargetReplaceListener(f));
        replaceElementIfExist(f.getVariable(), new ReplacementVisitor.CtVariableAccessVariableReplaceListener(f));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public void visitCtComment(final CtComment comment) {
        replaceInListIfExist(comment.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(comment));
        replaceInListIfExist(comment.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(comment));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public void visitCtJavaDoc(final CtJavaDoc javaDoc) {
        replaceInListIfExist(javaDoc.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(javaDoc));
        replaceInListIfExist(javaDoc.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(javaDoc));
        replaceInListIfExist(javaDoc.getTags(), new ReplacementVisitor.CtJavaDocTagsReplaceListener(javaDoc));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public void visitCtJavaDocTag(final CtJavaDocTag docTag) {
        replaceInListIfExist(docTag.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(docTag));
        replaceInListIfExist(docTag.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(docTag));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public void visitCtImport(final CtImport ctImport) {
        replaceElementIfExist(ctImport.getReference(), new ReplacementVisitor.CtImportReferenceReplaceListener(ctImport));
        replaceInListIfExist(ctImport.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(ctImport));
        replaceInListIfExist(ctImport.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(ctImport));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public void visitCtModule(CtModule module) {
        replaceInListIfExist(module.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(module));
        replaceInListIfExist(module.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(module));
        replaceInListIfExist(module.getModuleDirectives(), new ReplacementVisitor.CtModuleModuleDirectivesReplaceListener(module));
        replaceElementIfExist(module.getRootPackage(), new ReplacementVisitor.CtModuleRootPackageReplaceListener(module));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public void visitCtModuleReference(CtModuleReference moduleReference) {
        replaceInListIfExist(moduleReference.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(moduleReference));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public void visitCtPackageExport(CtPackageExport moduleExport) {
        replaceInListIfExist(moduleExport.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(moduleExport));
        replaceElementIfExist(moduleExport.getPackageReference(), new ReplacementVisitor.CtPackageExportPackageReferenceReplaceListener(moduleExport));
        replaceInListIfExist(moduleExport.getTargetExport(), new ReplacementVisitor.CtPackageExportTargetExportReplaceListener(moduleExport));
        replaceInListIfExist(moduleExport.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(moduleExport));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public void visitCtModuleRequirement(CtModuleRequirement moduleRequirement) {
        replaceInListIfExist(moduleRequirement.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(moduleRequirement));
        replaceElementIfExist(moduleRequirement.getModuleReference(), new ReplacementVisitor.CtModuleRequirementModuleReferenceReplaceListener(moduleRequirement));
        replaceInListIfExist(moduleRequirement.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(moduleRequirement));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public void visitCtProvidedService(CtProvidedService moduleProvidedService) {
        replaceInListIfExist(moduleProvidedService.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(moduleProvidedService));
        replaceElementIfExist(moduleProvidedService.getServiceType(), new ReplacementVisitor.CtProvidedServiceServiceTypeReplaceListener(moduleProvidedService));
        replaceInListIfExist(moduleProvidedService.getImplementationTypes(), new ReplacementVisitor.CtProvidedServiceImplementationTypesReplaceListener(moduleProvidedService));
        replaceInListIfExist(moduleProvidedService.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(moduleProvidedService));
    }

    // auto-generated, see spoon.generating.ReplacementVisitorGenerator
    @Override
    public void visitCtUsedService(CtUsedService usedService) {
        replaceInListIfExist(usedService.getComments(), new ReplacementVisitor.CtElementCommentsReplaceListener(usedService));
        replaceElementIfExist(usedService.getServiceType(), new ReplacementVisitor.CtUsedServiceServiceTypeReplaceListener(usedService));
        replaceInListIfExist(usedService.getAnnotations(), new ReplacementVisitor.CtElementAnnotationsReplaceListener(usedService));
    }
}

