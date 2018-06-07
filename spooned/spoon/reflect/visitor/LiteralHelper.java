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


import spoon.reflect.code.CtLiteral;
import spoon.reflect.cu.SourcePosition;


/**
 * Computes source code representation of the literal
 */
abstract class LiteralHelper {
    private LiteralHelper() {
    }

    /**
     *
     *
     * @param literal
     * 		to be converted literal
     * @return source code representation of the literal
     */
    public static <T> String getLiteralToken(CtLiteral<T> literal) {
        // the size of the string in the source code, the -1 is the size of the ' or " in the source code
        // if the string in the source is not the same as the string in the literal, the string may contains special characters
        // the size of the string in the source code, the -1 is the size of the ' or " in the source code
        // if the string in the source is not the same as the string in the literal, the string may contains special characters
        if ((literal.getValue()) == null) {
            return "null";
        }// the size of the string in the source code, the -1 is the size of the ' or " in the source code
        // if the string in the source is not the same as the string in the literal, the string may contains special characters
        // the size of the string in the source code, the -1 is the size of the ' or " in the source code
        // if the string in the source is not the same as the string in the literal, the string may contains special characters
        else
            if ((literal.getValue()) instanceof Long) {
                return (literal.getValue()) + "L";
            }else
                if ((literal.getValue()) instanceof Float) {
                    return (literal.getValue()) + "F";
                }else
                    if ((literal.getValue()) instanceof Character) {
                        boolean mayContainsSpecialCharacter = true;
                        spoon.reflect.cu.SourcePosition position = literal.getPosition();
                        if (position.isValidPosition()) {
                            int stringLength = ((position.getSourceEnd()) - (position.getSourceStart())) - 1;
                            mayContainsSpecialCharacter = stringLength != 1;
                        }
                        StringBuilder sb = new StringBuilder(10);
                        sb.append('\'');
                        LiteralHelper.appendCharLiteral(sb, ((Character) (literal.getValue())), mayContainsSpecialCharacter);
                        sb.append('\'');
                        return sb.toString();
                    }else
                        if ((literal.getValue()) instanceof String) {
                            boolean mayContainsSpecialCharacters = true;
                            spoon.reflect.cu.SourcePosition position = literal.getPosition();
                            if (position.isValidPosition()) {
                                int stringLength = ((position.getSourceEnd()) - (position.getSourceStart())) - 1;
                                mayContainsSpecialCharacters = (((String) (literal.getValue())).length()) != stringLength;
                            }
                            return ("\"" + (LiteralHelper.getStringLiteral(((String) (literal.getValue())), mayContainsSpecialCharacters))) + "\"";
                        }else
                            if ((literal.getValue()) instanceof Class) {
                                return ((Class<?>) (literal.getValue())).getName();
                            }else {
                                return literal.getValue().toString();
                            }





    }

    static void appendCharLiteral(StringBuilder sb, Character c, boolean mayContainsSpecialCharacter) {
        // $NON-NLS-1$
        // $NON-NLS-1$
        // $NON-NLS-1$
        // $NON-NLS-1$
        // $NON-NLS-1$
        // $NON-NLS-1$
        // $NON-NLS-1$
        // take care not to display the escape as a potential
        // real char
        // $NON-NLS-1$
        if (!mayContainsSpecialCharacter) {
            sb.append(c);
        }// $NON-NLS-1$
        // $NON-NLS-1$
        // $NON-NLS-1$
        // $NON-NLS-1$
        // $NON-NLS-1$
        // $NON-NLS-1$
        // $NON-NLS-1$
        // take care not to display the escape as a potential
        // real char
        // $NON-NLS-1$
        else
            if ((Character.UnicodeBlock.of(c)) != (Character.UnicodeBlock.BASIC_LATIN)) {
                if (c < 16) {
                    sb.append(("\\u000" + (Integer.toHexString(c))));
                }else
                    if (c < 256) {
                        sb.append(("\\u00" + (Integer.toHexString(c))));
                    }else
                        if (c < 4096) {
                            sb.append(("\\u0" + (Integer.toHexString(c))));
                        }else {
                            sb.append(("\\u" + (Integer.toHexString(c))));
                        }


            }else {
                switch (c) {
                    case '\b' :
                        sb.append("\\b");
                        break;
                    case '\t' :
                        sb.append("\\t");
                        break;
                    case '\n' :
                        sb.append("\\n");
                        break;
                    case '\f' :
                        sb.append("\\f");
                        break;
                    case '\r' :
                        sb.append("\\r");
                        break;
                    case '\"' :
                        sb.append("\\\"");
                        break;
                    case '\'' :
                        sb.append("\\\'");
                        break;
                    case '\\' :
                        sb.append("\\\\");
                        break;
                    default :
                        sb.append((Character.isISOControl(c) ? String.format("\\u%04x", ((int) (c))) : Character.toString(c)));
                }
            }

    }

    static String getStringLiteral(String value, boolean mayContainsSpecialCharacter) {
        if (!mayContainsSpecialCharacter) {
            return value;
        }else {
            StringBuilder sb = new StringBuilder(((value.length()) * 2));
            for (int i = 0; i < (value.length()); i++) {
                LiteralHelper.appendCharLiteral(sb, value.charAt(i), mayContainsSpecialCharacter);
            }
            return sb.toString();
        }
    }
}

