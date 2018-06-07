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
package spoon.support.visitor.clone;


import java.lang.annotation.Annotation;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBreak;
import spoon.reflect.code.CtCatchVariable;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtContinue;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtJavaDocTag;
import spoon.reflect.code.CtLambda;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtOperatorAssignment;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtAnonymousExecutable;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtModule;
import spoon.reflect.declaration.CtModuleRequirement;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtPackageExport;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeParameterReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtInheritanceScanner;


/**
 * Used to set all data in the cloned element.
 *
 * This class is generated automatically by the processor spoon.generating.CloneVisitorGenerator.
 */
public class CloneBuilder extends CtInheritanceScanner {
    public void copy(CtElement element, CtElement other) {
        this.setOther(other);
        this.scan(element);
    }

    public static <T extends CtElement> T build(CloneBuilder builder, CtElement element, CtElement other) {
        builder.setOther(other);
        builder.scan(element);
        return ((T) (builder.other));
    }

    private CtElement other;

    public void setOther(CtElement other) {
        this.other = other;
    }

    // auto-generated, see spoon.generating.CloneVisitorGenerator
    public <T> void visitCtCodeSnippetExpression(CtCodeSnippetExpression<T> e) {
        ((CtCodeSnippetExpression<T>) (other)).setValue(e.getValue());
        super.visitCtCodeSnippetExpression(e);
    }

    // auto-generated, see spoon.generating.CloneVisitorGenerator
    public void visitCtCodeSnippetStatement(CtCodeSnippetStatement e) {
        ((CtCodeSnippetStatement) (other)).setValue(e.getValue());
        super.visitCtCodeSnippetStatement(e);
    }

    /**
     * Scans an abstract element.
     */
    // auto-generated, see spoon.generating.CloneVisitorGenerator
    public void scanCtElement(CtElement e) {
        ((CtElement) (other)).setPosition(e.getPosition());
        ((CtElement) (other)).setImplicit(e.isImplicit());
        super.scanCtElement(e);
    }

    /**
     * Scans an abstract named element.
     */
    // auto-generated, see spoon.generating.CloneVisitorGenerator
    public void scanCtNamedElement(CtNamedElement e) {
        ((CtNamedElement) (other)).setSimpleName(e.getSimpleName());
        super.scanCtNamedElement(e);
    }

    /**
     * Scans an abstract reference.
     */
    // auto-generated, see spoon.generating.CloneVisitorGenerator
    public void scanCtReference(CtReference reference) {
        ((CtReference) (other)).setSimpleName(reference.getSimpleName());
        super.scanCtReference(reference);
    }

    /**
     * Scans an abstract statement.
     */
    // auto-generated, see spoon.generating.CloneVisitorGenerator
    public void scanCtStatement(CtStatement s) {
        ((CtStatement) (other)).setLabel(s.getLabel());
        super.scanCtStatement(s);
    }

    /**
     * Scans an abstract type.
     */
    // auto-generated, see spoon.generating.CloneVisitorGenerator
    public <T> void scanCtType(CtType<T> type) {
        ((CtType<T>) (other)).setModifiers(type.getModifiers());
        ((CtType<T>) (other)).setShadow(type.isShadow());
        super.scanCtType(type);
    }

    // auto-generated, see spoon.generating.CloneVisitorGenerator
    public <T, A extends T> void visitCtOperatorAssignment(CtOperatorAssignment<T, A> e) {
        ((CtOperatorAssignment<T, A>) (other)).setKind(e.getKind());
        super.visitCtOperatorAssignment(e);
    }

    // auto-generated, see spoon.generating.CloneVisitorGenerator
    public <A extends Annotation> void visitCtAnnotation(CtAnnotation<A> e) {
        ((CtAnnotation<A>) (other)).setShadow(e.isShadow());
        super.visitCtAnnotation(e);
    }

    // auto-generated, see spoon.generating.CloneVisitorGenerator
    public void visitCtAnonymousExecutable(CtAnonymousExecutable e) {
        ((CtAnonymousExecutable) (other)).setModifiers(e.getModifiers());
        super.visitCtAnonymousExecutable(e);
    }

    // auto-generated, see spoon.generating.CloneVisitorGenerator
    public <T> void visitCtBinaryOperator(CtBinaryOperator<T> e) {
        ((CtBinaryOperator<T>) (other)).setKind(e.getKind());
        super.visitCtBinaryOperator(e);
    }

    // auto-generated, see spoon.generating.CloneVisitorGenerator
    public void visitCtBreak(CtBreak e) {
        ((CtBreak) (other)).setTargetLabel(e.getTargetLabel());
        super.visitCtBreak(e);
    }

    // auto-generated, see spoon.generating.CloneVisitorGenerator
    public <T> void visitCtConstructor(CtConstructor<T> e) {
        ((CtConstructor<T>) (other)).setModifiers(e.getModifiers());
        ((CtConstructor<T>) (other)).setShadow(e.isShadow());
        super.visitCtConstructor(e);
    }

    // auto-generated, see spoon.generating.CloneVisitorGenerator
    public void visitCtContinue(CtContinue e) {
        ((CtContinue) (other)).setTargetLabel(e.getTargetLabel());
        super.visitCtContinue(e);
    }

    // auto-generated, see spoon.generating.CloneVisitorGenerator
    public <T> void visitCtExecutableReference(CtExecutableReference<T> e) {
        ((CtExecutableReference<T>) (other)).setStatic(e.isStatic());
        super.visitCtExecutableReference(e);
    }

    // auto-generated, see spoon.generating.CloneVisitorGenerator
    public <T> void visitCtField(CtField<T> e) {
        ((CtField<T>) (other)).setModifiers(e.getModifiers());
        ((CtField<T>) (other)).setShadow(e.isShadow());
        super.visitCtField(e);
    }

    // auto-generated, see spoon.generating.CloneVisitorGenerator
    public <T> void visitCtFieldReference(CtFieldReference<T> e) {
        ((CtFieldReference<T>) (other)).setFinal(e.isFinal());
        ((CtFieldReference<T>) (other)).setStatic(e.isStatic());
        super.visitCtFieldReference(e);
    }

    // auto-generated, see spoon.generating.CloneVisitorGenerator
    public <T> void visitCtInvocation(CtInvocation<T> e) {
        ((CtInvocation<T>) (other)).setLabel(e.getLabel());
        super.visitCtInvocation(e);
    }

    // auto-generated, see spoon.generating.CloneVisitorGenerator
    public <T> void visitCtLiteral(CtLiteral<T> e) {
        ((CtLiteral<T>) (other)).setValue(e.getValue());
        super.visitCtLiteral(e);
    }

    // auto-generated, see spoon.generating.CloneVisitorGenerator
    public <T> void visitCtLocalVariable(CtLocalVariable<T> e) {
        ((CtLocalVariable<T>) (other)).setSimpleName(e.getSimpleName());
        ((CtLocalVariable<T>) (other)).setModifiers(e.getModifiers());
        super.visitCtLocalVariable(e);
    }

    // auto-generated, see spoon.generating.CloneVisitorGenerator
    public <T> void visitCtCatchVariable(CtCatchVariable<T> e) {
        ((CtCatchVariable<T>) (other)).setSimpleName(e.getSimpleName());
        ((CtCatchVariable<T>) (other)).setModifiers(e.getModifiers());
        super.visitCtCatchVariable(e);
    }

    // auto-generated, see spoon.generating.CloneVisitorGenerator
    public <T> void visitCtMethod(CtMethod<T> e) {
        ((CtMethod<T>) (other)).setDefaultMethod(e.isDefaultMethod());
        ((CtMethod<T>) (other)).setModifiers(e.getModifiers());
        ((CtMethod<T>) (other)).setShadow(e.isShadow());
        super.visitCtMethod(e);
    }

    // auto-generated, see spoon.generating.CloneVisitorGenerator
    @Override
    public <T> void visitCtConstructorCall(CtConstructorCall<T> e) {
        ((CtConstructorCall<T>) (other)).setLabel(e.getLabel());
        super.visitCtConstructorCall(e);
    }

    // auto-generated, see spoon.generating.CloneVisitorGenerator
    @Override
    public <T> void visitCtLambda(CtLambda<T> e) {
        ((CtLambda<T>) (other)).setSimpleName(e.getSimpleName());
        super.visitCtLambda(e);
    }

    // auto-generated, see spoon.generating.CloneVisitorGenerator
    public <T, A extends T> void visitCtOperatorAssignement(CtOperatorAssignment<T, A> assignment) {
        ((CtOperatorAssignment<T, A>) (other)).setKind(assignment.getKind());
        super.visitCtOperatorAssignement(assignment);
    }

    // auto-generated, see spoon.generating.CloneVisitorGenerator
    public void visitCtPackage(CtPackage e) {
        ((CtPackage) (other)).setShadow(e.isShadow());
        super.visitCtPackage(e);
    }

    // auto-generated, see spoon.generating.CloneVisitorGenerator
    public <T> void visitCtParameter(CtParameter<T> e) {
        ((CtParameter<T>) (other)).setVarArgs(e.isVarArgs());
        ((CtParameter<T>) (other)).setModifiers(e.getModifiers());
        ((CtParameter<T>) (other)).setShadow(e.isShadow());
        super.visitCtParameter(e);
    }

    // auto-generated, see spoon.generating.CloneVisitorGenerator
    public void visitCtTypeParameterReference(CtTypeParameterReference e) {
        ((CtTypeParameterReference) (other)).setUpper(e.isUpper());
        super.visitCtTypeParameterReference(e);
    }

    // auto-generated, see spoon.generating.CloneVisitorGenerator
    public <T> void visitCtTypeReference(CtTypeReference<T> e) {
        ((CtTypeReference<T>) (other)).setShadow(e.isShadow());
        super.visitCtTypeReference(e);
    }

    // auto-generated, see spoon.generating.CloneVisitorGenerator
    public <T> void visitCtUnaryOperator(CtUnaryOperator<T> e) {
        ((CtUnaryOperator<T>) (other)).setKind(e.getKind());
        ((CtUnaryOperator<T>) (other)).setLabel(e.getLabel());
        super.visitCtUnaryOperator(e);
    }

    // auto-generated, see spoon.generating.CloneVisitorGenerator
    @Override
    public void visitCtComment(CtComment e) {
        ((CtComment) (other)).setContent(e.getContent());
        ((CtComment) (other)).setCommentType(e.getCommentType());
        super.visitCtComment(e);
    }

    // auto-generated, see spoon.generating.CloneVisitorGenerator
    @Override
    public void visitCtJavaDocTag(CtJavaDocTag e) {
        ((CtJavaDocTag) (other)).setType(e.getType());
        ((CtJavaDocTag) (other)).setContent(e.getContent());
        ((CtJavaDocTag) (other)).setParam(e.getParam());
        super.visitCtJavaDocTag(e);
    }

    // auto-generated, see spoon.generating.CloneVisitorGenerator
    @Override
    public void visitCtModule(CtModule module) {
        ((CtModule) (other)).setIsOpenModule(module.isOpenModule());
        super.visitCtModule(module);
    }

    // auto-generated, see spoon.generating.CloneVisitorGenerator
    @Override
    public void visitCtPackageExport(CtPackageExport moduleExport) {
        ((CtPackageExport) (other)).setOpenedPackage(moduleExport.isOpenedPackage());
        super.visitCtPackageExport(moduleExport);
    }

    // auto-generated, see spoon.generating.CloneVisitorGenerator
    @Override
    public void visitCtModuleRequirement(CtModuleRequirement moduleRequirement) {
        ((CtModuleRequirement) (other)).setRequiresModifiers(moduleRequirement.getRequiresModifiers());
        super.visitCtModuleRequirement(moduleRequirement);
    }
}

