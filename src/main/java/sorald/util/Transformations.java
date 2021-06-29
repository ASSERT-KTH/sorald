package sorald.util;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.UnaryOperatorKind;

/** Reusable transformations for use across the entire project. */
public class Transformations {

    private Transformations() {}

    /**
     * Negate an expression, i.e. turn `expr` into `!expr`.
     *
     * @param expr Expression to negate
     * @param <T> Type of the expression
     * @return The negation of the input expression
     */
    public static <T> CtUnaryOperator<T> not(CtExpression<T> expr) {
        CtUnaryOperator<T> operator = expr.getFactory().createUnaryOperator();
        operator.setKind(UnaryOperatorKind.NOT);
        operator.setOperand(expr);
        return operator;
    }
}
