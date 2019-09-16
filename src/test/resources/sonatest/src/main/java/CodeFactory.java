/**
 * Copyright (C) 2006-2019 INRIA and contributors
 *
 * Spoon is available either under the terms of the MIT License (see LICENSE-MIT.txt) of the Cecill-C License (see LICENSE-CECILL-C.txt). You as the user are entitled to choose the terms under which to adopt Spoon.
 */
package spoon.reflect.factory;
import spoon.SpoonException;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;
import java.util.Arrays;

public class CodeFactory extends SubFactory {
	public <T> CtNewClass<T> createNewClass(CtType<T> superClass, CtExpression<?>...parameters) {
		CtNewClass<T> ctNewClass = factory.Core().createNewClass();
		CtConstructor<T> constructor = ((CtClass) superClass).getConstructor(Arrays.stream(parameters).map(x -> x.getType()).toArray(CtTypeReference[]::new));
		/*
		CtTypeReference[] paranetersType = new CtTypeReference[parameters.length];
		for(int i = 0; i < parameters.length; i++) {
			paranetersType[i] = parameters[i].getType();
		}
		CtConstructor<T> constructor = ((CtClass) superClass).getConstructor(paranetersType);
		*/
		if (constructor == null) {
			throw new SpoonException("no appropriate constructor for these parameters " + parameters.toString());// Noncompliant
		}
		return ctNewClass;
	}
}
