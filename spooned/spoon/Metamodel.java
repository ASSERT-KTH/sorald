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
package spoon;


import java.util.HashSet;
import java.util.Set;
import spoon.reflect.code.BinaryOperatorKind;
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
import spoon.reflect.code.CtCFlowBreak;
import spoon.reflect.code.CtCase;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtCatchVariable;
import spoon.reflect.code.CtCodeElement;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtContinue;
import spoon.reflect.code.CtDo;
import spoon.reflect.code.CtExecutableReferenceExpression;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtJavaDoc;
import spoon.reflect.code.CtJavaDocTag;
import spoon.reflect.code.CtLabelledFlowBreak;
import spoon.reflect.code.CtLambda;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtLoop;
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
import spoon.reflect.code.UnaryOperatorKind;
import spoon.reflect.declaration.CtAnnotatedElementType;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtAnnotationMethod;
import spoon.reflect.declaration.CtAnnotationType;
import spoon.reflect.declaration.CtAnonymousExecutable;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtCodeSnippet;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtEnum;
import spoon.reflect.declaration.CtEnumValue;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtFormalTypeDeclarer;
import spoon.reflect.declaration.CtImport;
import spoon.reflect.declaration.CtImportKind;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtModifiable;
import spoon.reflect.declaration.CtModule;
import spoon.reflect.declaration.CtModuleDirective;
import spoon.reflect.declaration.CtModuleRequirement;
import spoon.reflect.declaration.CtMultiTypedElement;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtPackageExport;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtProvidedService;
import spoon.reflect.declaration.CtShadowable;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeInformation;
import spoon.reflect.declaration.CtTypeMember;
import spoon.reflect.declaration.CtTypeParameter;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.declaration.CtUsedService;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.declaration.ParentNotInitializedException;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.FactoryImpl;
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
import spoon.support.DefaultCoreFactory;
import spoon.support.StandardEnvironment;


/**
 * This class enables to reason on the Spoon metamodel directly
 */
public class Metamodel {
    private Metamodel() {
    }

    /**
     * Returns all interfaces of the Spoon metamodel.
     * This method is stateless for sake of maintenance.
     * If you need to call it several times, you should store the result.
     */
    public static Set<CtType<?>> getAllMetamodelInterfaces() {
        Set<CtType<?>> result = new HashSet<>();
        Factory factory = new FactoryImpl(new DefaultCoreFactory(), new StandardEnvironment());
        result.add(factory.Type().get(BinaryOperatorKind.class));
        result.add(factory.Type().get(CtAbstractInvocation.class));
        result.add(factory.Type().get(CtAnnotationFieldAccess.class));
        result.add(factory.Type().get(CtArrayAccess.class));
        result.add(factory.Type().get(CtArrayRead.class));
        result.add(factory.Type().get(CtArrayWrite.class));
        result.add(factory.Type().get(CtAssert.class));
        result.add(factory.Type().get(CtAssignment.class));
        result.add(factory.Type().get(CtBinaryOperator.class));
        result.add(factory.Type().get(CtBlock.class));
        result.add(factory.Type().get(CtBodyHolder.class));
        result.add(factory.Type().get(CtBreak.class));
        result.add(factory.Type().get(CtCFlowBreak.class));
        result.add(factory.Type().get(CtCase.class));
        result.add(factory.Type().get(CtCatch.class));
        result.add(factory.Type().get(CtCatchVariable.class));
        result.add(factory.Type().get(CtCodeElement.class));
        result.add(factory.Type().get(CtCodeSnippetExpression.class));
        result.add(factory.Type().get(CtCodeSnippetStatement.class));
        result.add(factory.Type().get(CtComment.class));
        result.add(factory.Type().get(CtConditional.class));
        result.add(factory.Type().get(CtConstructorCall.class));
        result.add(factory.Type().get(CtContinue.class));
        result.add(factory.Type().get(CtDo.class));
        result.add(factory.Type().get(CtExecutableReferenceExpression.class));
        result.add(factory.Type().get(CtExpression.class));
        result.add(factory.Type().get(CtFieldAccess.class));
        result.add(factory.Type().get(CtFieldRead.class));
        result.add(factory.Type().get(CtFieldWrite.class));
        result.add(factory.Type().get(CtFor.class));
        result.add(factory.Type().get(CtForEach.class));
        result.add(factory.Type().get(CtIf.class));
        result.add(factory.Type().get(CtInvocation.class));
        result.add(factory.Type().get(CtJavaDoc.class));
        result.add(factory.Type().get(CtJavaDocTag.class));
        result.add(factory.Type().get(CtLabelledFlowBreak.class));
        result.add(factory.Type().get(CtLambda.class));
        result.add(factory.Type().get(CtLiteral.class));
        result.add(factory.Type().get(CtLocalVariable.class));
        result.add(factory.Type().get(CtLoop.class));
        result.add(factory.Type().get(CtNewArray.class));
        result.add(factory.Type().get(CtNewClass.class));
        result.add(factory.Type().get(CtOperatorAssignment.class));
        result.add(factory.Type().get(CtRHSReceiver.class));
        result.add(factory.Type().get(CtReturn.class));
        result.add(factory.Type().get(CtStatement.class));
        result.add(factory.Type().get(CtStatementList.class));
        result.add(factory.Type().get(CtSuperAccess.class));
        result.add(factory.Type().get(CtSwitch.class));
        result.add(factory.Type().get(CtSynchronized.class));
        result.add(factory.Type().get(CtTargetedExpression.class));
        result.add(factory.Type().get(CtThisAccess.class));
        result.add(factory.Type().get(CtThrow.class));
        result.add(factory.Type().get(CtTry.class));
        result.add(factory.Type().get(CtTryWithResource.class));
        result.add(factory.Type().get(CtTypeAccess.class));
        result.add(factory.Type().get(CtUnaryOperator.class));
        result.add(factory.Type().get(CtVariableAccess.class));
        result.add(factory.Type().get(CtVariableRead.class));
        result.add(factory.Type().get(CtVariableWrite.class));
        result.add(factory.Type().get(CtWhile.class));
        result.add(factory.Type().get(UnaryOperatorKind.class));
        result.add(factory.Type().get(CtAnnotatedElementType.class));
        result.add(factory.Type().get(CtAnnotation.class));
        result.add(factory.Type().get(CtAnnotationMethod.class));
        result.add(factory.Type().get(CtAnnotationType.class));
        result.add(factory.Type().get(CtAnonymousExecutable.class));
        result.add(factory.Type().get(CtClass.class));
        result.add(factory.Type().get(CtCodeSnippet.class));
        result.add(factory.Type().get(CtConstructor.class));
        result.add(factory.Type().get(CtElement.class));
        result.add(factory.Type().get(CtEnum.class));
        result.add(factory.Type().get(CtEnumValue.class));
        result.add(factory.Type().get(CtExecutable.class));
        result.add(factory.Type().get(CtField.class));
        result.add(factory.Type().get(CtFormalTypeDeclarer.class));
        result.add(factory.Type().get(CtInterface.class));
        result.add(factory.Type().get(CtMethod.class));
        result.add(factory.Type().get(CtModifiable.class));
        result.add(factory.Type().get(CtMultiTypedElement.class));
        result.add(factory.Type().get(CtNamedElement.class));
        result.add(factory.Type().get(CtPackage.class));
        result.add(factory.Type().get(CtParameter.class));
        result.add(factory.Type().get(CtShadowable.class));
        result.add(factory.Type().get(CtType.class));
        result.add(factory.Type().get(CtTypeInformation.class));
        result.add(factory.Type().get(CtTypeMember.class));
        result.add(factory.Type().get(CtTypeParameter.class));
        result.add(factory.Type().get(CtTypedElement.class));
        result.add(factory.Type().get(CtVariable.class));
        result.add(factory.Type().get(ModifierKind.class));
        result.add(factory.Type().get(ParentNotInitializedException.class));
        result.add(factory.Type().get(CtActualTypeContainer.class));
        result.add(factory.Type().get(CtArrayTypeReference.class));
        result.add(factory.Type().get(CtCatchVariableReference.class));
        result.add(factory.Type().get(CtExecutableReference.class));
        result.add(factory.Type().get(CtFieldReference.class));
        result.add(factory.Type().get(CtIntersectionTypeReference.class));
        result.add(factory.Type().get(CtLocalVariableReference.class));
        result.add(factory.Type().get(CtPackageReference.class));
        result.add(factory.Type().get(CtParameterReference.class));
        result.add(factory.Type().get(CtReference.class));
        result.add(factory.Type().get(CtTypeParameterReference.class));
        result.add(factory.Type().get(CtTypeReference.class));
        result.add(factory.Type().get(CtUnboundVariableReference.class));
        result.add(factory.Type().get(CtVariableReference.class));
        result.add(factory.Type().get(CtWildcardReference.class));
        result.add(factory.Type().get(CtImport.class));
        result.add(factory.Type().get(CtImportKind.class));
        result.add(factory.Type().get(CtModule.class));
        result.add(factory.Type().get(CtModuleRequirement.class));
        result.add(factory.Type().get(CtPackageExport.class));
        result.add(factory.Type().get(CtProvidedService.class));
        result.add(factory.Type().get(CtModuleReference.class));
        result.add(factory.Type().get(CtUsedService.class));
        result.add(factory.Type().get(CtModuleDirective.class));
        return result;
    }
}

