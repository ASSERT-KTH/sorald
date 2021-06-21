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

public class CodeFactory {

	public void createNewClass(CtExpression<?>...parameters) {
		Arrays.stream(parameters).map(x -> x.getType());

		// This was recognized as noncompliant by sonar-java 5.14, but not anymore with 6.9.0
		parameters.toString(); // Noncompliant
	}

}
