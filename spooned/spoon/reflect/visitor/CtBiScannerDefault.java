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
package spoon.reflect.visitor;


import java.lang.annotation.Annotation;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import spoon.reflect.code.CtAnnotationFieldAccess;
import spoon.reflect.code.CtArrayRead;
import spoon.reflect.code.CtArrayWrite;
import spoon.reflect.code.CtAssert;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
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
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtSuperAccess;
import spoon.reflect.code.CtSwitch;
import spoon.reflect.code.CtSynchronized;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtThrow;
import spoon.reflect.code.CtTry;
import spoon.reflect.code.CtTryWithResource;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.code.CtUnaryOperator;
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
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtImport;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtModule;
import spoon.reflect.declaration.CtModuleRequirement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtPackageExport;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtProvidedService;
import spoon.reflect.declaration.CtTypeParameter;
import spoon.reflect.declaration.CtUsedService;
import spoon.reflect.path.CtRole;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtCatchVariableReference;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtIntersectionTypeReference;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.reference.CtModuleReference;
import spoon.reflect.reference.CtPackageReference;
import spoon.reflect.reference.CtParameterReference;
import spoon.reflect.reference.CtTypeParameterReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtUnboundVariableReference;
import spoon.reflect.reference.CtWildcardReference;


/**
 * This visitor implements a deep-search scan on the model for 2 elements.
 *
 * Ensures that all children nodes are visited once, a visit means three method
 * calls, one call to "enter", one call to "exit" and one call to biScan.
 *
 * This class is generated automatically by the processor spoon.generating.CtBiScannerGenerator.
 *
 * Is used by EqualsVisitor.
 */
// autogenerated by CtBiScannerGenerator
public class CtBiScannerDefault extends CtAbstractBiScanner {
    protected Deque<CtElement> stack = new ArrayDeque<>();

    protected void enter(CtElement e) {
    }

    protected void exit(CtElement e) {
    }

    public void biScan(CtElement element, CtElement other) {
        if (other == null) {
            return;
        }
        stack.push(other);
        try {
            element.accept(this);
        } finally {
            stack.pop();
        }
    }

    public void biScan(CtRole role, CtElement element, CtElement other) {
        biScan(element, other);
    }

    protected void biScan(CtRole role, Collection<? extends CtElement> elements, Collection<? extends CtElement> others) {
        for (Iterator<? extends CtElement> firstIt = elements.iterator(), secondIt = others.iterator(); (firstIt.hasNext()) && (secondIt.hasNext());) {
            biScan(role, firstIt.next(), secondIt.next());
        }
    }

    // autogenerated by CtBiScannerGenerator
    public <A extends Annotation> void visitCtAnnotation(final CtAnnotation<A> annotation) {
        CtAnnotation other = ((CtAnnotation) (this.stack.peek()));
        enter(annotation);
        biScan(CtRole.TYPE, annotation.getType(), other.getType());
        biScan(CtRole.COMMENT, annotation.getComments(), other.getComments());
        biScan(CtRole.ANNOTATION_TYPE, annotation.getAnnotationType(), other.getAnnotationType());
        biScan(CtRole.ANNOTATION, annotation.getAnnotations(), other.getAnnotations());
        biScan(CtRole.VALUE, annotation.getValues().values(), other.getValues().values());
        exit(annotation);
    }

    // autogenerated by CtBiScannerGenerator
    public <A extends Annotation> void visitCtAnnotationType(final CtAnnotationType<A> annotationType) {
        CtAnnotationType other = ((CtAnnotationType) (this.stack.peek()));
        enter(annotationType);
        biScan(CtRole.ANNOTATION, annotationType.getAnnotations(), other.getAnnotations());
        biScan(CtRole.TYPE_MEMBER, annotationType.getTypeMembers(), other.getTypeMembers());
        biScan(CtRole.COMMENT, annotationType.getComments(), other.getComments());
        exit(annotationType);
    }

    // autogenerated by CtBiScannerGenerator
    public void visitCtAnonymousExecutable(final CtAnonymousExecutable anonymousExec) {
        CtAnonymousExecutable other = ((CtAnonymousExecutable) (this.stack.peek()));
        enter(anonymousExec);
        biScan(CtRole.ANNOTATION, anonymousExec.getAnnotations(), other.getAnnotations());
        biScan(CtRole.BODY, anonymousExec.getBody(), other.getBody());
        biScan(CtRole.COMMENT, anonymousExec.getComments(), other.getComments());
        exit(anonymousExec);
    }

    // autogenerated by CtBiScannerGenerator
    @Override
    public <T> void visitCtArrayRead(final CtArrayRead<T> arrayRead) {
        CtArrayRead other = ((CtArrayRead) (this.stack.peek()));
        enter(arrayRead);
        biScan(CtRole.ANNOTATION, arrayRead.getAnnotations(), other.getAnnotations());
        biScan(CtRole.TYPE, arrayRead.getType(), other.getType());
        biScan(CtRole.CAST, arrayRead.getTypeCasts(), other.getTypeCasts());
        biScan(CtRole.TARGET, arrayRead.getTarget(), other.getTarget());
        biScan(CtRole.EXPRESSION, arrayRead.getIndexExpression(), other.getIndexExpression());
        biScan(CtRole.COMMENT, arrayRead.getComments(), other.getComments());
        exit(arrayRead);
    }

    // autogenerated by CtBiScannerGenerator
    @Override
    public <T> void visitCtArrayWrite(final CtArrayWrite<T> arrayWrite) {
        CtArrayWrite other = ((CtArrayWrite) (this.stack.peek()));
        enter(arrayWrite);
        biScan(CtRole.ANNOTATION, arrayWrite.getAnnotations(), other.getAnnotations());
        biScan(CtRole.TYPE, arrayWrite.getType(), other.getType());
        biScan(CtRole.CAST, arrayWrite.getTypeCasts(), other.getTypeCasts());
        biScan(CtRole.TARGET, arrayWrite.getTarget(), other.getTarget());
        biScan(CtRole.EXPRESSION, arrayWrite.getIndexExpression(), other.getIndexExpression());
        biScan(CtRole.COMMENT, arrayWrite.getComments(), other.getComments());
        exit(arrayWrite);
    }

    // autogenerated by CtBiScannerGenerator
    public <T> void visitCtArrayTypeReference(final CtArrayTypeReference<T> reference) {
        CtArrayTypeReference other = ((CtArrayTypeReference) (this.stack.peek()));
        enter(reference);
        biScan(CtRole.PACKAGE_REF, reference.getPackage(), other.getPackage());
        biScan(CtRole.DECLARING_TYPE, reference.getDeclaringType(), other.getDeclaringType());
        biScan(CtRole.TYPE, reference.getComponentType(), other.getComponentType());
        biScan(CtRole.TYPE_ARGUMENT, reference.getActualTypeArguments(), other.getActualTypeArguments());
        biScan(CtRole.ANNOTATION, reference.getAnnotations(), other.getAnnotations());
        exit(reference);
    }

    // autogenerated by CtBiScannerGenerator
    public <T> void visitCtAssert(final CtAssert<T> asserted) {
        CtAssert other = ((CtAssert) (this.stack.peek()));
        enter(asserted);
        biScan(CtRole.ANNOTATION, asserted.getAnnotations(), other.getAnnotations());
        biScan(CtRole.CONDITION, asserted.getAssertExpression(), other.getAssertExpression());
        biScan(CtRole.EXPRESSION, asserted.getExpression(), other.getExpression());
        biScan(CtRole.COMMENT, asserted.getComments(), other.getComments());
        exit(asserted);
    }

    // autogenerated by CtBiScannerGenerator
    public <T, A extends T> void visitCtAssignment(final CtAssignment<T, A> assignement) {
        CtAssignment other = ((CtAssignment) (this.stack.peek()));
        enter(assignement);
        biScan(CtRole.ANNOTATION, assignement.getAnnotations(), other.getAnnotations());
        biScan(CtRole.TYPE, assignement.getType(), other.getType());
        biScan(CtRole.CAST, assignement.getTypeCasts(), other.getTypeCasts());
        biScan(CtRole.ASSIGNED, assignement.getAssigned(), other.getAssigned());
        biScan(CtRole.ASSIGNMENT, assignement.getAssignment(), other.getAssignment());
        biScan(CtRole.COMMENT, assignement.getComments(), other.getComments());
        exit(assignement);
    }

    // autogenerated by CtBiScannerGenerator
    public <T> void visitCtBinaryOperator(final CtBinaryOperator<T> operator) {
        CtBinaryOperator other = ((CtBinaryOperator) (this.stack.peek()));
        enter(operator);
        biScan(CtRole.ANNOTATION, operator.getAnnotations(), other.getAnnotations());
        biScan(CtRole.TYPE, operator.getType(), other.getType());
        biScan(CtRole.CAST, operator.getTypeCasts(), other.getTypeCasts());
        biScan(CtRole.LEFT_OPERAND, operator.getLeftHandOperand(), other.getLeftHandOperand());
        biScan(CtRole.RIGHT_OPERAND, operator.getRightHandOperand(), other.getRightHandOperand());
        biScan(CtRole.COMMENT, operator.getComments(), other.getComments());
        exit(operator);
    }

    // autogenerated by CtBiScannerGenerator
    public <R> void visitCtBlock(final CtBlock<R> block) {
        CtBlock other = ((CtBlock) (this.stack.peek()));
        enter(block);
        biScan(CtRole.ANNOTATION, block.getAnnotations(), other.getAnnotations());
        biScan(CtRole.STATEMENT, block.getStatements(), other.getStatements());
        biScan(CtRole.COMMENT, block.getComments(), other.getComments());
        exit(block);
    }

    // autogenerated by CtBiScannerGenerator
    public void visitCtBreak(final CtBreak breakStatement) {
        CtBreak other = ((CtBreak) (this.stack.peek()));
        enter(breakStatement);
        biScan(CtRole.ANNOTATION, breakStatement.getAnnotations(), other.getAnnotations());
        biScan(CtRole.COMMENT, breakStatement.getComments(), other.getComments());
        exit(breakStatement);
    }

    // autogenerated by CtBiScannerGenerator
    public <S> void visitCtCase(final CtCase<S> caseStatement) {
        CtCase other = ((CtCase) (this.stack.peek()));
        enter(caseStatement);
        biScan(CtRole.ANNOTATION, caseStatement.getAnnotations(), other.getAnnotations());
        biScan(CtRole.EXPRESSION, caseStatement.getCaseExpression(), other.getCaseExpression());
        biScan(CtRole.STATEMENT, caseStatement.getStatements(), other.getStatements());
        biScan(CtRole.COMMENT, caseStatement.getComments(), other.getComments());
        exit(caseStatement);
    }

    // autogenerated by CtBiScannerGenerator
    public void visitCtCatch(final CtCatch catchBlock) {
        CtCatch other = ((CtCatch) (this.stack.peek()));
        enter(catchBlock);
        biScan(CtRole.ANNOTATION, catchBlock.getAnnotations(), other.getAnnotations());
        biScan(CtRole.PARAMETER, catchBlock.getParameter(), other.getParameter());
        biScan(CtRole.BODY, catchBlock.getBody(), other.getBody());
        biScan(CtRole.COMMENT, catchBlock.getComments(), other.getComments());
        exit(catchBlock);
    }

    // autogenerated by CtBiScannerGenerator
    public <T> void visitCtClass(final CtClass<T> ctClass) {
        CtClass other = ((CtClass) (this.stack.peek()));
        enter(ctClass);
        biScan(CtRole.ANNOTATION, ctClass.getAnnotations(), other.getAnnotations());
        biScan(CtRole.SUPER_TYPE, ctClass.getSuperclass(), other.getSuperclass());
        biScan(CtRole.INTERFACE, ctClass.getSuperInterfaces(), other.getSuperInterfaces());
        biScan(CtRole.TYPE_PARAMETER, ctClass.getFormalCtTypeParameters(), other.getFormalCtTypeParameters());
        biScan(CtRole.TYPE_MEMBER, ctClass.getTypeMembers(), other.getTypeMembers());
        biScan(CtRole.COMMENT, ctClass.getComments(), other.getComments());
        exit(ctClass);
    }

    // autogenerated by CtBiScannerGenerator
    @Override
    public void visitCtTypeParameter(CtTypeParameter typeParameter) {
        CtTypeParameter other = ((CtTypeParameter) (this.stack.peek()));
        enter(typeParameter);
        biScan(CtRole.ANNOTATION, typeParameter.getAnnotations(), other.getAnnotations());
        biScan(CtRole.SUPER_TYPE, typeParameter.getSuperclass(), other.getSuperclass());
        biScan(CtRole.COMMENT, typeParameter.getComments(), other.getComments());
        exit(typeParameter);
    }

    // autogenerated by CtBiScannerGenerator
    public <T> void visitCtConditional(final CtConditional<T> conditional) {
        CtConditional other = ((CtConditional) (this.stack.peek()));
        enter(conditional);
        biScan(CtRole.TYPE, conditional.getType(), other.getType());
        biScan(CtRole.ANNOTATION, conditional.getAnnotations(), other.getAnnotations());
        biScan(CtRole.CONDITION, conditional.getCondition(), other.getCondition());
        biScan(CtRole.THEN, conditional.getThenExpression(), other.getThenExpression());
        biScan(CtRole.ELSE, conditional.getElseExpression(), other.getElseExpression());
        biScan(CtRole.COMMENT, conditional.getComments(), other.getComments());
        biScan(CtRole.CAST, conditional.getTypeCasts(), other.getTypeCasts());
        exit(conditional);
    }

    // autogenerated by CtBiScannerGenerator
    public <T> void visitCtConstructor(final CtConstructor<T> c) {
        CtConstructor other = ((CtConstructor) (this.stack.peek()));
        enter(c);
        biScan(CtRole.ANNOTATION, c.getAnnotations(), other.getAnnotations());
        biScan(CtRole.PARAMETER, c.getParameters(), other.getParameters());
        biScan(CtRole.THROWN, c.getThrownTypes(), other.getThrownTypes());
        biScan(CtRole.TYPE_PARAMETER, c.getFormalCtTypeParameters(), other.getFormalCtTypeParameters());
        biScan(CtRole.BODY, c.getBody(), other.getBody());
        biScan(CtRole.COMMENT, c.getComments(), other.getComments());
        exit(c);
    }

    // autogenerated by CtBiScannerGenerator
    public void visitCtContinue(final CtContinue continueStatement) {
        CtContinue other = ((CtContinue) (this.stack.peek()));
        enter(continueStatement);
        biScan(CtRole.ANNOTATION, continueStatement.getAnnotations(), other.getAnnotations());
        biScan(CtRole.COMMENT, continueStatement.getComments(), other.getComments());
        exit(continueStatement);
    }

    // autogenerated by CtBiScannerGenerator
    public void visitCtDo(final CtDo doLoop) {
        CtDo other = ((CtDo) (this.stack.peek()));
        enter(doLoop);
        biScan(CtRole.ANNOTATION, doLoop.getAnnotations(), other.getAnnotations());
        biScan(CtRole.EXPRESSION, doLoop.getLoopingExpression(), other.getLoopingExpression());
        biScan(CtRole.BODY, doLoop.getBody(), other.getBody());
        biScan(CtRole.COMMENT, doLoop.getComments(), other.getComments());
        exit(doLoop);
    }

    // autogenerated by CtBiScannerGenerator
    public <T extends Enum<?>> void visitCtEnum(final CtEnum<T> ctEnum) {
        CtEnum other = ((CtEnum) (this.stack.peek()));
        enter(ctEnum);
        biScan(CtRole.ANNOTATION, ctEnum.getAnnotations(), other.getAnnotations());
        biScan(CtRole.INTERFACE, ctEnum.getSuperInterfaces(), other.getSuperInterfaces());
        biScan(CtRole.TYPE_MEMBER, ctEnum.getTypeMembers(), other.getTypeMembers());
        biScan(CtRole.VALUE, ctEnum.getEnumValues(), other.getEnumValues());
        biScan(CtRole.COMMENT, ctEnum.getComments(), other.getComments());
        exit(ctEnum);
    }

    // autogenerated by CtBiScannerGenerator
    public <T> void visitCtExecutableReference(final CtExecutableReference<T> reference) {
        CtExecutableReference other = ((CtExecutableReference) (this.stack.peek()));
        enter(reference);
        biScan(CtRole.DECLARING_TYPE, reference.getDeclaringType(), other.getDeclaringType());
        biScan(CtRole.TYPE, reference.getType(), other.getType());
        biScan(CtRole.ARGUMENT_TYPE, reference.getParameters(), other.getParameters());
        biScan(CtRole.TYPE_ARGUMENT, reference.getActualTypeArguments(), other.getActualTypeArguments());
        biScan(CtRole.ANNOTATION, reference.getAnnotations(), other.getAnnotations());
        biScan(CtRole.COMMENT, reference.getComments(), other.getComments());
        exit(reference);
    }

    // autogenerated by CtBiScannerGenerator
    public <T> void visitCtField(final CtField<T> f) {
        CtField other = ((CtField) (this.stack.peek()));
        enter(f);
        biScan(CtRole.ANNOTATION, f.getAnnotations(), other.getAnnotations());
        biScan(CtRole.TYPE, f.getType(), other.getType());
        biScan(CtRole.DEFAULT_EXPRESSION, f.getDefaultExpression(), other.getDefaultExpression());
        biScan(CtRole.COMMENT, f.getComments(), other.getComments());
        exit(f);
    }

    // autogenerated by CtBiScannerGenerator
    @Override
    public <T> void visitCtEnumValue(final CtEnumValue<T> enumValue) {
        CtEnumValue other = ((CtEnumValue) (this.stack.peek()));
        enter(enumValue);
        biScan(CtRole.ANNOTATION, enumValue.getAnnotations(), other.getAnnotations());
        biScan(CtRole.TYPE, enumValue.getType(), other.getType());
        biScan(CtRole.DEFAULT_EXPRESSION, enumValue.getDefaultExpression(), other.getDefaultExpression());
        biScan(CtRole.COMMENT, enumValue.getComments(), other.getComments());
        exit(enumValue);
    }

    // autogenerated by CtBiScannerGenerator
    @Override
    public <T> void visitCtThisAccess(final CtThisAccess<T> thisAccess) {
        CtThisAccess other = ((CtThisAccess) (this.stack.peek()));
        enter(thisAccess);
        biScan(CtRole.COMMENT, thisAccess.getComments(), other.getComments());
        biScan(CtRole.ANNOTATION, thisAccess.getAnnotations(), other.getAnnotations());
        biScan(CtRole.TYPE, thisAccess.getType(), other.getType());
        biScan(CtRole.CAST, thisAccess.getTypeCasts(), other.getTypeCasts());
        biScan(CtRole.TARGET, thisAccess.getTarget(), other.getTarget());
        exit(thisAccess);
    }

    // autogenerated by CtBiScannerGenerator
    public <T> void visitCtAnnotationFieldAccess(final CtAnnotationFieldAccess<T> annotationFieldAccess) {
        CtAnnotationFieldAccess other = ((CtAnnotationFieldAccess) (this.stack.peek()));
        enter(annotationFieldAccess);
        biScan(CtRole.COMMENT, annotationFieldAccess.getComments(), other.getComments());
        biScan(CtRole.ANNOTATION, annotationFieldAccess.getAnnotations(), other.getAnnotations());
        biScan(CtRole.CAST, annotationFieldAccess.getTypeCasts(), other.getTypeCasts());
        biScan(CtRole.TARGET, annotationFieldAccess.getTarget(), other.getTarget());
        biScan(CtRole.VARIABLE, annotationFieldAccess.getVariable(), other.getVariable());
        exit(annotationFieldAccess);
    }

    // autogenerated by CtBiScannerGenerator
    public <T> void visitCtFieldReference(final CtFieldReference<T> reference) {
        CtFieldReference other = ((CtFieldReference) (this.stack.peek()));
        enter(reference);
        biScan(CtRole.DECLARING_TYPE, reference.getDeclaringType(), other.getDeclaringType());
        biScan(CtRole.TYPE, reference.getType(), other.getType());
        biScan(CtRole.ANNOTATION, reference.getAnnotations(), other.getAnnotations());
        exit(reference);
    }

    // autogenerated by CtBiScannerGenerator
    public void visitCtFor(final CtFor forLoop) {
        CtFor other = ((CtFor) (this.stack.peek()));
        enter(forLoop);
        biScan(CtRole.ANNOTATION, forLoop.getAnnotations(), other.getAnnotations());
        biScan(CtRole.FOR_INIT, forLoop.getForInit(), other.getForInit());
        biScan(CtRole.EXPRESSION, forLoop.getExpression(), other.getExpression());
        biScan(CtRole.FOR_UPDATE, forLoop.getForUpdate(), other.getForUpdate());
        biScan(CtRole.BODY, forLoop.getBody(), other.getBody());
        biScan(CtRole.COMMENT, forLoop.getComments(), other.getComments());
        exit(forLoop);
    }

    // autogenerated by CtBiScannerGenerator
    public void visitCtForEach(final CtForEach foreach) {
        CtForEach other = ((CtForEach) (this.stack.peek()));
        enter(foreach);
        biScan(CtRole.ANNOTATION, foreach.getAnnotations(), other.getAnnotations());
        biScan(CtRole.FOREACH_VARIABLE, foreach.getVariable(), other.getVariable());
        biScan(CtRole.EXPRESSION, foreach.getExpression(), other.getExpression());
        biScan(CtRole.BODY, foreach.getBody(), other.getBody());
        biScan(CtRole.COMMENT, foreach.getComments(), other.getComments());
        exit(foreach);
    }

    // autogenerated by CtBiScannerGenerator
    public void visitCtIf(final CtIf ifElement) {
        CtIf other = ((CtIf) (this.stack.peek()));
        enter(ifElement);
        biScan(CtRole.ANNOTATION, ifElement.getAnnotations(), other.getAnnotations());
        biScan(CtRole.CONDITION, ifElement.getCondition(), other.getCondition());
        biScan(CtRole.THEN, ((CtStatement) (ifElement.getThenStatement())), other.getThenStatement());
        biScan(CtRole.ELSE, ((CtStatement) (ifElement.getElseStatement())), other.getElseStatement());
        biScan(CtRole.COMMENT, ifElement.getComments(), other.getComments());
        exit(ifElement);
    }

    // autogenerated by CtBiScannerGenerator
    public <T> void visitCtInterface(final CtInterface<T> intrface) {
        CtInterface other = ((CtInterface) (this.stack.peek()));
        enter(intrface);
        biScan(CtRole.ANNOTATION, intrface.getAnnotations(), other.getAnnotations());
        biScan(CtRole.INTERFACE, intrface.getSuperInterfaces(), other.getSuperInterfaces());
        biScan(CtRole.TYPE_PARAMETER, intrface.getFormalCtTypeParameters(), other.getFormalCtTypeParameters());
        biScan(CtRole.TYPE_MEMBER, intrface.getTypeMembers(), other.getTypeMembers());
        biScan(CtRole.COMMENT, intrface.getComments(), other.getComments());
        exit(intrface);
    }

    // autogenerated by CtBiScannerGenerator
    public <T> void visitCtInvocation(final CtInvocation<T> invocation) {
        CtInvocation other = ((CtInvocation) (this.stack.peek()));
        enter(invocation);
        biScan(CtRole.ANNOTATION, invocation.getAnnotations(), other.getAnnotations());
        biScan(CtRole.CAST, invocation.getTypeCasts(), other.getTypeCasts());
        biScan(CtRole.TARGET, invocation.getTarget(), other.getTarget());
        biScan(CtRole.EXECUTABLE_REF, invocation.getExecutable(), other.getExecutable());
        biScan(CtRole.ARGUMENT, invocation.getArguments(), other.getArguments());
        biScan(CtRole.COMMENT, invocation.getComments(), other.getComments());
        exit(invocation);
    }

    // autogenerated by CtBiScannerGenerator
    public <T> void visitCtLiteral(final CtLiteral<T> literal) {
        CtLiteral other = ((CtLiteral) (this.stack.peek()));
        enter(literal);
        biScan(CtRole.ANNOTATION, literal.getAnnotations(), other.getAnnotations());
        biScan(CtRole.TYPE, literal.getType(), other.getType());
        biScan(CtRole.CAST, literal.getTypeCasts(), other.getTypeCasts());
        biScan(CtRole.COMMENT, literal.getComments(), other.getComments());
        exit(literal);
    }

    // autogenerated by CtBiScannerGenerator
    public <T> void visitCtLocalVariable(final CtLocalVariable<T> localVariable) {
        CtLocalVariable other = ((CtLocalVariable) (this.stack.peek()));
        enter(localVariable);
        biScan(CtRole.ANNOTATION, localVariable.getAnnotations(), other.getAnnotations());
        biScan(CtRole.TYPE, localVariable.getType(), other.getType());
        biScan(CtRole.DEFAULT_EXPRESSION, localVariable.getDefaultExpression(), other.getDefaultExpression());
        biScan(CtRole.COMMENT, localVariable.getComments(), other.getComments());
        exit(localVariable);
    }

    // autogenerated by CtBiScannerGenerator
    public <T> void visitCtLocalVariableReference(final CtLocalVariableReference<T> reference) {
        CtLocalVariableReference other = ((CtLocalVariableReference) (this.stack.peek()));
        enter(reference);
        biScan(CtRole.TYPE, reference.getType(), other.getType());
        biScan(CtRole.ANNOTATION, reference.getAnnotations(), other.getAnnotations());
        exit(reference);
    }

    // autogenerated by CtBiScannerGenerator
    public <T> void visitCtCatchVariable(final CtCatchVariable<T> catchVariable) {
        CtCatchVariable other = ((CtCatchVariable) (this.stack.peek()));
        enter(catchVariable);
        biScan(CtRole.COMMENT, catchVariable.getComments(), other.getComments());
        biScan(CtRole.ANNOTATION, catchVariable.getAnnotations(), other.getAnnotations());
        biScan(CtRole.MULTI_TYPE, catchVariable.getMultiTypes(), other.getMultiTypes());
        exit(catchVariable);
    }

    // autogenerated by CtBiScannerGenerator
    public <T> void visitCtCatchVariableReference(final CtCatchVariableReference<T> reference) {
        CtCatchVariableReference other = ((CtCatchVariableReference) (this.stack.peek()));
        enter(reference);
        biScan(CtRole.TYPE, reference.getType(), other.getType());
        biScan(CtRole.ANNOTATION, reference.getAnnotations(), other.getAnnotations());
        exit(reference);
    }

    // autogenerated by CtBiScannerGenerator
    public <T> void visitCtMethod(final CtMethod<T> m) {
        CtMethod other = ((CtMethod) (this.stack.peek()));
        enter(m);
        biScan(CtRole.ANNOTATION, m.getAnnotations(), other.getAnnotations());
        biScan(CtRole.TYPE_PARAMETER, m.getFormalCtTypeParameters(), other.getFormalCtTypeParameters());
        biScan(CtRole.TYPE, m.getType(), other.getType());
        biScan(CtRole.PARAMETER, m.getParameters(), other.getParameters());
        biScan(CtRole.THROWN, m.getThrownTypes(), other.getThrownTypes());
        biScan(CtRole.BODY, m.getBody(), other.getBody());
        biScan(CtRole.COMMENT, m.getComments(), other.getComments());
        exit(m);
    }

    // autogenerated by CtBiScannerGenerator
    @Override
    public <T> void visitCtAnnotationMethod(CtAnnotationMethod<T> annotationMethod) {
        CtAnnotationMethod other = ((CtAnnotationMethod) (this.stack.peek()));
        enter(annotationMethod);
        biScan(CtRole.ANNOTATION, annotationMethod.getAnnotations(), other.getAnnotations());
        biScan(CtRole.TYPE, annotationMethod.getType(), other.getType());
        biScan(CtRole.DEFAULT_EXPRESSION, annotationMethod.getDefaultExpression(), other.getDefaultExpression());
        biScan(CtRole.COMMENT, annotationMethod.getComments(), other.getComments());
        exit(annotationMethod);
    }

    // autogenerated by CtBiScannerGenerator
    public <T> void visitCtNewArray(final CtNewArray<T> newArray) {
        CtNewArray other = ((CtNewArray) (this.stack.peek()));
        enter(newArray);
        biScan(CtRole.ANNOTATION, newArray.getAnnotations(), other.getAnnotations());
        biScan(CtRole.TYPE, newArray.getType(), other.getType());
        biScan(CtRole.CAST, newArray.getTypeCasts(), other.getTypeCasts());
        biScan(CtRole.EXPRESSION, newArray.getElements(), other.getElements());
        biScan(CtRole.DIMENSION, newArray.getDimensionExpressions(), other.getDimensionExpressions());
        biScan(CtRole.COMMENT, newArray.getComments(), other.getComments());
        exit(newArray);
    }

    // autogenerated by CtBiScannerGenerator
    @Override
    public <T> void visitCtConstructorCall(final CtConstructorCall<T> ctConstructorCall) {
        CtConstructorCall other = ((CtConstructorCall) (this.stack.peek()));
        enter(ctConstructorCall);
        biScan(CtRole.ANNOTATION, ctConstructorCall.getAnnotations(), other.getAnnotations());
        biScan(CtRole.CAST, ctConstructorCall.getTypeCasts(), other.getTypeCasts());
        biScan(CtRole.EXECUTABLE_REF, ctConstructorCall.getExecutable(), other.getExecutable());
        biScan(CtRole.TARGET, ctConstructorCall.getTarget(), other.getTarget());
        biScan(CtRole.ARGUMENT, ctConstructorCall.getArguments(), other.getArguments());
        biScan(CtRole.COMMENT, ctConstructorCall.getComments(), other.getComments());
        exit(ctConstructorCall);
    }

    // autogenerated by CtBiScannerGenerator
    public <T> void visitCtNewClass(final CtNewClass<T> newClass) {
        CtNewClass other = ((CtNewClass) (this.stack.peek()));
        enter(newClass);
        biScan(CtRole.ANNOTATION, newClass.getAnnotations(), other.getAnnotations());
        biScan(CtRole.CAST, newClass.getTypeCasts(), other.getTypeCasts());
        biScan(CtRole.EXECUTABLE_REF, newClass.getExecutable(), other.getExecutable());
        biScan(CtRole.TARGET, newClass.getTarget(), other.getTarget());
        biScan(CtRole.ARGUMENT, newClass.getArguments(), other.getArguments());
        biScan(CtRole.NESTED_TYPE, newClass.getAnonymousClass(), other.getAnonymousClass());
        biScan(CtRole.COMMENT, newClass.getComments(), other.getComments());
        exit(newClass);
    }

    // autogenerated by CtBiScannerGenerator
    @Override
    public <T> void visitCtLambda(final CtLambda<T> lambda) {
        CtLambda other = ((CtLambda) (this.stack.peek()));
        enter(lambda);
        biScan(CtRole.ANNOTATION, lambda.getAnnotations(), other.getAnnotations());
        biScan(CtRole.TYPE, lambda.getType(), other.getType());
        biScan(CtRole.CAST, lambda.getTypeCasts(), other.getTypeCasts());
        biScan(CtRole.PARAMETER, lambda.getParameters(), other.getParameters());
        biScan(CtRole.BODY, lambda.getBody(), other.getBody());
        biScan(CtRole.EXPRESSION, lambda.getExpression(), other.getExpression());
        biScan(CtRole.COMMENT, lambda.getComments(), other.getComments());
        exit(lambda);
    }

    // autogenerated by CtBiScannerGenerator
    @Override
    public <T, E extends CtExpression<?>> void visitCtExecutableReferenceExpression(final CtExecutableReferenceExpression<T, E> expression) {
        CtExecutableReferenceExpression other = ((CtExecutableReferenceExpression) (this.stack.peek()));
        enter(expression);
        biScan(CtRole.COMMENT, expression.getComments(), other.getComments());
        biScan(CtRole.ANNOTATION, expression.getAnnotations(), other.getAnnotations());
        biScan(CtRole.TYPE, expression.getType(), other.getType());
        biScan(CtRole.CAST, expression.getTypeCasts(), other.getTypeCasts());
        biScan(CtRole.EXECUTABLE_REF, expression.getExecutable(), other.getExecutable());
        biScan(CtRole.TARGET, expression.getTarget(), other.getTarget());
        exit(expression);
    }

    // autogenerated by CtBiScannerGenerator
    public <T, A extends T> void visitCtOperatorAssignment(final CtOperatorAssignment<T, A> assignment) {
        CtOperatorAssignment other = ((CtOperatorAssignment) (this.stack.peek()));
        enter(assignment);
        biScan(CtRole.ANNOTATION, assignment.getAnnotations(), other.getAnnotations());
        biScan(CtRole.TYPE, assignment.getType(), other.getType());
        biScan(CtRole.CAST, assignment.getTypeCasts(), other.getTypeCasts());
        biScan(CtRole.ASSIGNED, assignment.getAssigned(), other.getAssigned());
        biScan(CtRole.ASSIGNMENT, assignment.getAssignment(), other.getAssignment());
        biScan(CtRole.COMMENT, assignment.getComments(), other.getComments());
        exit(assignment);
    }

    // autogenerated by CtBiScannerGenerator
    public void visitCtPackage(final CtPackage ctPackage) {
        CtPackage other = ((CtPackage) (this.stack.peek()));
        enter(ctPackage);
        biScan(CtRole.ANNOTATION, ctPackage.getAnnotations(), other.getAnnotations());
        biScan(CtRole.SUB_PACKAGE, ctPackage.getPackages(), other.getPackages());
        biScan(CtRole.CONTAINED_TYPE, ctPackage.getTypes(), other.getTypes());
        biScan(CtRole.COMMENT, ctPackage.getComments(), other.getComments());
        exit(ctPackage);
    }

    // autogenerated by CtBiScannerGenerator
    public void visitCtPackageReference(final CtPackageReference reference) {
        CtPackageReference other = ((CtPackageReference) (this.stack.peek()));
        enter(reference);
        biScan(CtRole.ANNOTATION, reference.getAnnotations(), other.getAnnotations());
        exit(reference);
    }

    // autogenerated by CtBiScannerGenerator
    public <T> void visitCtParameter(final CtParameter<T> parameter) {
        CtParameter other = ((CtParameter) (this.stack.peek()));
        enter(parameter);
        biScan(CtRole.ANNOTATION, parameter.getAnnotations(), other.getAnnotations());
        biScan(CtRole.TYPE, parameter.getType(), other.getType());
        biScan(CtRole.COMMENT, parameter.getComments(), other.getComments());
        exit(parameter);
    }

    // autogenerated by CtBiScannerGenerator
    public <T> void visitCtParameterReference(final CtParameterReference<T> reference) {
        CtParameterReference other = ((CtParameterReference) (this.stack.peek()));
        enter(reference);
        biScan(CtRole.TYPE, reference.getType(), other.getType());
        biScan(CtRole.ANNOTATION, reference.getAnnotations(), other.getAnnotations());
        exit(reference);
    }

    // autogenerated by CtBiScannerGenerator
    public <R> void visitCtReturn(final CtReturn<R> returnStatement) {
        CtReturn other = ((CtReturn) (this.stack.peek()));
        enter(returnStatement);
        biScan(CtRole.ANNOTATION, returnStatement.getAnnotations(), other.getAnnotations());
        biScan(CtRole.EXPRESSION, returnStatement.getReturnedExpression(), other.getReturnedExpression());
        biScan(CtRole.COMMENT, returnStatement.getComments(), other.getComments());
        exit(returnStatement);
    }

    // autogenerated by CtBiScannerGenerator
    public <R> void visitCtStatementList(final CtStatementList statements) {
        CtStatementList other = ((CtStatementList) (this.stack.peek()));
        enter(statements);
        biScan(CtRole.ANNOTATION, statements.getAnnotations(), other.getAnnotations());
        biScan(CtRole.STATEMENT, statements.getStatements(), other.getStatements());
        biScan(CtRole.COMMENT, statements.getComments(), other.getComments());
        exit(statements);
    }

    // autogenerated by CtBiScannerGenerator
    public <S> void visitCtSwitch(final CtSwitch<S> switchStatement) {
        CtSwitch other = ((CtSwitch) (this.stack.peek()));
        enter(switchStatement);
        biScan(CtRole.ANNOTATION, switchStatement.getAnnotations(), other.getAnnotations());
        biScan(CtRole.EXPRESSION, switchStatement.getSelector(), other.getSelector());
        biScan(CtRole.CASE, switchStatement.getCases(), other.getCases());
        biScan(CtRole.COMMENT, switchStatement.getComments(), other.getComments());
        exit(switchStatement);
    }

    // autogenerated by CtBiScannerGenerator
    public void visitCtSynchronized(final CtSynchronized synchro) {
        CtSynchronized other = ((CtSynchronized) (this.stack.peek()));
        enter(synchro);
        biScan(CtRole.ANNOTATION, synchro.getAnnotations(), other.getAnnotations());
        biScan(CtRole.EXPRESSION, synchro.getExpression(), other.getExpression());
        biScan(CtRole.BODY, synchro.getBlock(), other.getBlock());
        biScan(CtRole.COMMENT, synchro.getComments(), other.getComments());
        exit(synchro);
    }

    // autogenerated by CtBiScannerGenerator
    public void visitCtThrow(final CtThrow throwStatement) {
        CtThrow other = ((CtThrow) (this.stack.peek()));
        enter(throwStatement);
        biScan(CtRole.ANNOTATION, throwStatement.getAnnotations(), other.getAnnotations());
        biScan(CtRole.EXPRESSION, throwStatement.getThrownExpression(), other.getThrownExpression());
        biScan(CtRole.COMMENT, throwStatement.getComments(), other.getComments());
        exit(throwStatement);
    }

    // autogenerated by CtBiScannerGenerator
    public void visitCtTry(final CtTry tryBlock) {
        CtTry other = ((CtTry) (this.stack.peek()));
        enter(tryBlock);
        biScan(CtRole.ANNOTATION, tryBlock.getAnnotations(), other.getAnnotations());
        biScan(CtRole.BODY, tryBlock.getBody(), other.getBody());
        biScan(CtRole.CATCH, tryBlock.getCatchers(), other.getCatchers());
        biScan(CtRole.FINALIZER, tryBlock.getFinalizer(), other.getFinalizer());
        biScan(CtRole.COMMENT, tryBlock.getComments(), other.getComments());
        exit(tryBlock);
    }

    // autogenerated by CtBiScannerGenerator
    @Override
    public void visitCtTryWithResource(final CtTryWithResource tryWithResource) {
        CtTryWithResource other = ((CtTryWithResource) (this.stack.peek()));
        enter(tryWithResource);
        biScan(CtRole.ANNOTATION, tryWithResource.getAnnotations(), other.getAnnotations());
        biScan(CtRole.TRY_RESOURCE, tryWithResource.getResources(), other.getResources());
        biScan(CtRole.BODY, tryWithResource.getBody(), other.getBody());
        biScan(CtRole.CATCH, tryWithResource.getCatchers(), other.getCatchers());
        biScan(CtRole.FINALIZER, tryWithResource.getFinalizer(), other.getFinalizer());
        biScan(CtRole.COMMENT, tryWithResource.getComments(), other.getComments());
        exit(tryWithResource);
    }

    // autogenerated by CtBiScannerGenerator
    public void visitCtTypeParameterReference(final CtTypeParameterReference ref) {
        CtTypeParameterReference other = ((CtTypeParameterReference) (this.stack.peek()));
        enter(ref);
        biScan(CtRole.PACKAGE_REF, ref.getPackage(), other.getPackage());
        biScan(CtRole.DECLARING_TYPE, ref.getDeclaringType(), other.getDeclaringType());
        biScan(CtRole.ANNOTATION, ref.getAnnotations(), other.getAnnotations());
        biScan(CtRole.BOUNDING_TYPE, ref.getBoundingType(), other.getBoundingType());
        exit(ref);
    }

    // autogenerated by CtBiScannerGenerator
    @Override
    public void visitCtWildcardReference(CtWildcardReference wildcardReference) {
        CtWildcardReference other = ((CtWildcardReference) (this.stack.peek()));
        enter(wildcardReference);
        biScan(CtRole.PACKAGE_REF, wildcardReference.getPackage(), other.getPackage());
        biScan(CtRole.DECLARING_TYPE, wildcardReference.getDeclaringType(), other.getDeclaringType());
        biScan(CtRole.ANNOTATION, wildcardReference.getAnnotations(), other.getAnnotations());
        biScan(CtRole.BOUNDING_TYPE, wildcardReference.getBoundingType(), other.getBoundingType());
        exit(wildcardReference);
    }

    // autogenerated by CtBiScannerGenerator
    @Override
    public <T> void visitCtIntersectionTypeReference(final CtIntersectionTypeReference<T> reference) {
        CtIntersectionTypeReference other = ((CtIntersectionTypeReference) (this.stack.peek()));
        enter(reference);
        biScan(CtRole.PACKAGE_REF, reference.getPackage(), other.getPackage());
        biScan(CtRole.DECLARING_TYPE, reference.getDeclaringType(), other.getDeclaringType());
        // TypeReferenceTest fails if actual type arguments are really not set-able on CtIntersectionTypeReference
        biScan(CtRole.TYPE_ARGUMENT, reference.getActualTypeArguments(), other.getActualTypeArguments());
        biScan(CtRole.ANNOTATION, reference.getAnnotations(), other.getAnnotations());
        biScan(CtRole.BOUND, reference.getBounds(), other.getBounds());
        exit(reference);
    }

    // autogenerated by CtBiScannerGenerator
    public <T> void visitCtTypeReference(final CtTypeReference<T> reference) {
        CtTypeReference other = ((CtTypeReference) (this.stack.peek()));
        enter(reference);
        biScan(CtRole.PACKAGE_REF, reference.getPackage(), other.getPackage());
        biScan(CtRole.DECLARING_TYPE, reference.getDeclaringType(), other.getDeclaringType());
        biScan(CtRole.TYPE_ARGUMENT, reference.getActualTypeArguments(), other.getActualTypeArguments());
        biScan(CtRole.ANNOTATION, reference.getAnnotations(), other.getAnnotations());
        biScan(CtRole.COMMENT, reference.getComments(), other.getComments());
        exit(reference);
    }

    // autogenerated by CtBiScannerGenerator
    @Override
    public <T> void visitCtTypeAccess(final CtTypeAccess<T> typeAccess) {
        CtTypeAccess other = ((CtTypeAccess) (this.stack.peek()));
        enter(typeAccess);
        biScan(CtRole.ANNOTATION, typeAccess.getAnnotations(), other.getAnnotations());
        biScan(CtRole.CAST, typeAccess.getTypeCasts(), other.getTypeCasts());
        biScan(CtRole.ACCESSED_TYPE, typeAccess.getAccessedType(), other.getAccessedType());
        biScan(CtRole.COMMENT, typeAccess.getComments(), other.getComments());
        exit(typeAccess);
    }

    // autogenerated by CtBiScannerGenerator
    public <T> void visitCtUnaryOperator(final CtUnaryOperator<T> operator) {
        CtUnaryOperator other = ((CtUnaryOperator) (this.stack.peek()));
        enter(operator);
        biScan(CtRole.ANNOTATION, operator.getAnnotations(), other.getAnnotations());
        biScan(CtRole.TYPE, operator.getType(), other.getType());
        biScan(CtRole.CAST, operator.getTypeCasts(), other.getTypeCasts());
        biScan(CtRole.EXPRESSION, operator.getOperand(), other.getOperand());
        biScan(CtRole.COMMENT, operator.getComments(), other.getComments());
        exit(operator);
    }

    // autogenerated by CtBiScannerGenerator
    @Override
    public <T> void visitCtVariableRead(final CtVariableRead<T> variableRead) {
        CtVariableRead other = ((CtVariableRead) (this.stack.peek()));
        enter(variableRead);
        biScan(CtRole.ANNOTATION, variableRead.getAnnotations(), other.getAnnotations());
        biScan(CtRole.CAST, variableRead.getTypeCasts(), other.getTypeCasts());
        biScan(CtRole.VARIABLE, variableRead.getVariable(), other.getVariable());
        biScan(CtRole.COMMENT, variableRead.getComments(), other.getComments());
        exit(variableRead);
    }

    // autogenerated by CtBiScannerGenerator
    @Override
    public <T> void visitCtVariableWrite(final CtVariableWrite<T> variableWrite) {
        CtVariableWrite other = ((CtVariableWrite) (this.stack.peek()));
        enter(variableWrite);
        biScan(CtRole.ANNOTATION, variableWrite.getAnnotations(), other.getAnnotations());
        biScan(CtRole.CAST, variableWrite.getTypeCasts(), other.getTypeCasts());
        biScan(CtRole.VARIABLE, variableWrite.getVariable(), other.getVariable());
        biScan(CtRole.COMMENT, variableWrite.getComments(), other.getComments());
        exit(variableWrite);
    }

    // autogenerated by CtBiScannerGenerator
    public void visitCtWhile(final CtWhile whileLoop) {
        CtWhile other = ((CtWhile) (this.stack.peek()));
        enter(whileLoop);
        biScan(CtRole.ANNOTATION, whileLoop.getAnnotations(), other.getAnnotations());
        biScan(CtRole.EXPRESSION, whileLoop.getLoopingExpression(), other.getLoopingExpression());
        biScan(CtRole.BODY, whileLoop.getBody(), other.getBody());
        biScan(CtRole.COMMENT, whileLoop.getComments(), other.getComments());
        exit(whileLoop);
    }

    // autogenerated by CtBiScannerGenerator
    public <T> void visitCtCodeSnippetExpression(final CtCodeSnippetExpression<T> expression) {
        CtCodeSnippetExpression other = ((CtCodeSnippetExpression) (this.stack.peek()));
        enter(expression);
        biScan(CtRole.TYPE, expression.getType(), other.getType());
        biScan(CtRole.COMMENT, expression.getComments(), other.getComments());
        biScan(CtRole.ANNOTATION, expression.getAnnotations(), other.getAnnotations());
        biScan(CtRole.CAST, expression.getTypeCasts(), other.getTypeCasts());
        exit(expression);
    }

    // autogenerated by CtBiScannerGenerator
    public void visitCtCodeSnippetStatement(final CtCodeSnippetStatement statement) {
        CtCodeSnippetStatement other = ((CtCodeSnippetStatement) (this.stack.peek()));
        enter(statement);
        biScan(CtRole.COMMENT, statement.getComments(), other.getComments());
        biScan(CtRole.ANNOTATION, statement.getAnnotations(), other.getAnnotations());
        exit(statement);
    }

    // autogenerated by CtBiScannerGenerator
    public <T> void visitCtUnboundVariableReference(final CtUnboundVariableReference<T> reference) {
        CtUnboundVariableReference other = ((CtUnboundVariableReference) (this.stack.peek()));
        enter(reference);
        biScan(CtRole.TYPE, reference.getType(), other.getType());
        exit(reference);
    }

    // autogenerated by CtBiScannerGenerator
    @Override
    public <T> void visitCtFieldRead(final CtFieldRead<T> fieldRead) {
        CtFieldRead other = ((CtFieldRead) (this.stack.peek()));
        enter(fieldRead);
        biScan(CtRole.ANNOTATION, fieldRead.getAnnotations(), other.getAnnotations());
        biScan(CtRole.CAST, fieldRead.getTypeCasts(), other.getTypeCasts());
        biScan(CtRole.TARGET, fieldRead.getTarget(), other.getTarget());
        biScan(CtRole.VARIABLE, fieldRead.getVariable(), other.getVariable());
        biScan(CtRole.COMMENT, fieldRead.getComments(), other.getComments());
        exit(fieldRead);
    }

    // autogenerated by CtBiScannerGenerator
    @Override
    public <T> void visitCtFieldWrite(final CtFieldWrite<T> fieldWrite) {
        CtFieldWrite other = ((CtFieldWrite) (this.stack.peek()));
        enter(fieldWrite);
        biScan(CtRole.ANNOTATION, fieldWrite.getAnnotations(), other.getAnnotations());
        biScan(CtRole.CAST, fieldWrite.getTypeCasts(), other.getTypeCasts());
        biScan(CtRole.TARGET, fieldWrite.getTarget(), other.getTarget());
        biScan(CtRole.VARIABLE, fieldWrite.getVariable(), other.getVariable());
        biScan(CtRole.COMMENT, fieldWrite.getComments(), other.getComments());
        exit(fieldWrite);
    }

    // autogenerated by CtBiScannerGenerator
    @Override
    public <T> void visitCtSuperAccess(final CtSuperAccess<T> f) {
        CtSuperAccess other = ((CtSuperAccess) (this.stack.peek()));
        enter(f);
        biScan(CtRole.COMMENT, f.getComments(), other.getComments());
        biScan(CtRole.ANNOTATION, f.getAnnotations(), other.getAnnotations());
        biScan(CtRole.CAST, f.getTypeCasts(), other.getTypeCasts());
        biScan(CtRole.TARGET, f.getTarget(), other.getTarget());
        biScan(CtRole.VARIABLE, f.getVariable(), other.getVariable());
        exit(f);
    }

    // autogenerated by CtBiScannerGenerator
    @Override
    public void visitCtComment(final CtComment comment) {
        CtComment other = ((CtComment) (this.stack.peek()));
        enter(comment);
        biScan(CtRole.COMMENT, comment.getComments(), other.getComments());
        biScan(CtRole.ANNOTATION, comment.getAnnotations(), other.getAnnotations());
        exit(comment);
    }

    // autogenerated by CtBiScannerGenerator
    @Override
    public void visitCtJavaDoc(final CtJavaDoc javaDoc) {
        CtJavaDoc other = ((CtJavaDoc) (this.stack.peek()));
        enter(javaDoc);
        biScan(CtRole.COMMENT, javaDoc.getComments(), other.getComments());
        biScan(CtRole.ANNOTATION, javaDoc.getAnnotations(), other.getAnnotations());
        biScan(CtRole.COMMENT_TAG, javaDoc.getTags(), other.getTags());
        exit(javaDoc);
    }

    // autogenerated by CtBiScannerGenerator
    @Override
    public void visitCtJavaDocTag(final CtJavaDocTag docTag) {
        CtJavaDocTag other = ((CtJavaDocTag) (this.stack.peek()));
        enter(docTag);
        biScan(CtRole.COMMENT, docTag.getComments(), other.getComments());
        biScan(CtRole.ANNOTATION, docTag.getAnnotations(), other.getAnnotations());
        exit(docTag);
    }

    // autogenerated by CtBiScannerGenerator
    @Override
    public void visitCtImport(final CtImport ctImport) {
        CtImport other = ((CtImport) (this.stack.peek()));
        enter(ctImport);
        biScan(CtRole.IMPORT_REFERENCE, ctImport.getReference(), other.getReference());
        biScan(CtRole.ANNOTATION, ctImport.getAnnotations(), other.getAnnotations());
        biScan(CtRole.COMMENT, ctImport.getComments(), other.getComments());
        exit(ctImport);
    }

    // autogenerated by CtBiScannerGenerator
    @Override
    public void visitCtModule(CtModule module) {
        CtModule other = ((CtModule) (this.stack.peek()));
        enter(module);
        biScan(CtRole.COMMENT, module.getComments(), other.getComments());
        biScan(CtRole.ANNOTATION, module.getAnnotations(), other.getAnnotations());
        biScan(CtRole.MODULE_DIRECTIVE, module.getModuleDirectives(), other.getModuleDirectives());
        biScan(CtRole.SUB_PACKAGE, module.getRootPackage(), other.getRootPackage());
        exit(module);
    }

    // autogenerated by CtBiScannerGenerator
    @Override
    public void visitCtModuleReference(CtModuleReference moduleReference) {
        CtModuleReference other = ((CtModuleReference) (this.stack.peek()));
        enter(moduleReference);
        biScan(CtRole.ANNOTATION, moduleReference.getAnnotations(), other.getAnnotations());
        exit(moduleReference);
    }

    // autogenerated by CtBiScannerGenerator
    @Override
    public void visitCtPackageExport(CtPackageExport moduleExport) {
        CtPackageExport other = ((CtPackageExport) (this.stack.peek()));
        enter(moduleExport);
        biScan(CtRole.COMMENT, moduleExport.getComments(), other.getComments());
        biScan(CtRole.PACKAGE_REF, moduleExport.getPackageReference(), other.getPackageReference());
        biScan(CtRole.MODULE_REF, moduleExport.getTargetExport(), other.getTargetExport());
        biScan(CtRole.ANNOTATION, moduleExport.getAnnotations(), other.getAnnotations());
        exit(moduleExport);
    }

    // autogenerated by CtBiScannerGenerator
    @Override
    public void visitCtModuleRequirement(CtModuleRequirement moduleRequirement) {
        CtModuleRequirement other = ((CtModuleRequirement) (this.stack.peek()));
        enter(moduleRequirement);
        biScan(CtRole.COMMENT, moduleRequirement.getComments(), other.getComments());
        biScan(CtRole.MODULE_REF, moduleRequirement.getModuleReference(), other.getModuleReference());
        biScan(CtRole.ANNOTATION, moduleRequirement.getAnnotations(), other.getAnnotations());
        exit(moduleRequirement);
    }

    // autogenerated by CtBiScannerGenerator
    @Override
    public void visitCtProvidedService(CtProvidedService moduleProvidedService) {
        CtProvidedService other = ((CtProvidedService) (this.stack.peek()));
        enter(moduleProvidedService);
        biScan(CtRole.COMMENT, moduleProvidedService.getComments(), other.getComments());
        biScan(CtRole.SERVICE_TYPE, moduleProvidedService.getServiceType(), other.getServiceType());
        biScan(CtRole.IMPLEMENTATION_TYPE, moduleProvidedService.getImplementationTypes(), other.getImplementationTypes());
        biScan(CtRole.ANNOTATION, moduleProvidedService.getAnnotations(), other.getAnnotations());
        exit(moduleProvidedService);
    }

    // autogenerated by CtBiScannerGenerator
    @Override
    public void visitCtUsedService(CtUsedService usedService) {
        CtUsedService other = ((CtUsedService) (this.stack.peek()));
        enter(usedService);
        biScan(CtRole.COMMENT, usedService.getComments(), other.getComments());
        biScan(CtRole.SERVICE_TYPE, usedService.getServiceType(), other.getServiceType());
        biScan(CtRole.ANNOTATION, usedService.getAnnotations(), other.getAnnotations());
        exit(usedService);
    }
}

