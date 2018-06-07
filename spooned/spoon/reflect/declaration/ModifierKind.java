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
package spoon.reflect.declaration;


import java.util.Locale;


/**
 * Represents a modifier on the declaration of a program element such as a
 * class, method, or field.
 *
 * The order is important, because it is always pretty--printed is this order, enabling to have a JLS-compliant,
 * checkstyle compliant generated code (thanks to EnumSet used for modifiers).
 */
public enum ModifierKind {
    PUBLIC, PROTECTED, PRIVATE, ABSTRACT, STATIC, FINAL, TRANSIENT, VOLATILE, SYNCHRONIZED, NATIVE, STRICTFP;
    private String lowercase = null;// modifier name in lowercase


    /**
     * Returns this modifier's name in lowercase.
     */
    @Override
    public String toString() {
        if ((lowercase) == null) {
            lowercase = name().toLowerCase(Locale.US);
        }
        return lowercase;
    }
}

