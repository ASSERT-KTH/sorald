package sorald.processor;

import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtArrayAccess;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.factory.Factory;

// @spotless:off
/**
 * Any assignment with identical left and right expressions will be processed. If the identifier being used in the self-assignment exists as both a local variable and a field, then the left expression will be changed by adding `this.` at the beginning of the expression. In any other case, including cases where there are invocations or access to another class field, such as `objectA.b = objectA.b`, the assignment will be removed.
 *
 * Example:
 * ```diff
 * class SelfAssignement {
 *    int a,c = 0;
 *    int[] b = {0};
 *    int h = 0;
 *    int[] g = 0;
 *    SelfAssignementCheckB checkB = new SelfAssignementCheckB();
 * -  void method(int e,int h) {
 * -    a = a; // Noncompliant [[sc=7;ec=8]] {{Remove or correct this useless self-assignment.}}
 * -    this.a = this.a; // Noncompliant
 * -    b[0] = b[0]; // Noncompliant
 * -    b[fun()] = b[fun()]; // Noncompliant
 * -    int d = 0;
 * -    d = d; // Noncompliant
 * -    e = e; // Noncompliant
 *      int[] g = new int[]{ 0 };
 * -    g[fun()] = g[fun()]; // Noncompliant
 * -    h = h;
 * -    checkB.b = checkB.b; // Noncompliant
 * -    checkB.getSelf().foo = checkB.getSelf().foo; // Noncompliant
 * -  }
 * +  void method(int e,int h) {
 * +    int d = 0;
 *      int[] g = new int[]{ 0 };
 * +    this.g[fun()] = g[fun()];
 * +    this.h = h;
 * +  }
 *    int fun() {
 *      return 0;
 *    }
 * }
 * ```
 */
// @spotless:on
@ProcessorAnnotation(key = "S1656", description = "Variables should not be self-assigned")
public class SelfAssignementProcessor extends SoraldAbstractProcessor<CtAssignment<?, ?>> {

    @Override
    protected void repairInternal(CtAssignment<?, ?> element) {
        Factory factory = element.getFactory();
        CtType<?> type = element.getParent(CtType.class);

        CtThisAccess access = factory.createThisAccess(type.getReference());
        CtFieldRead<?> fieldRead = factory.createFieldRead();

        fieldRead.setTarget(access);
        CtExpression<?> leftExpression = element.getAssigned();
        CtExpression<?> rightExpression = element.getAssignment();
        CtExpression<?> leftExpression2Check;
        CtExpression<?> rightExpression2Check;
        if (leftExpression instanceof CtArrayAccess && rightExpression instanceof CtArrayAccess) {
            leftExpression2Check = ((CtArrayAccess) leftExpression).getTarget();
            rightExpression2Check = ((CtArrayAccess) rightExpression).getTarget();
        } else {
            leftExpression2Check = leftExpression;
            rightExpression2Check = rightExpression;
        }
        boolean instanceOfFieldAccess =
                leftExpression2Check instanceof CtFieldAccess
                        && rightExpression2Check
                                instanceof
                                CtFieldAccess; // True if no identical local variable present
        boolean instanceOfVariableAccess =
                leftExpression2Check instanceof CtVariableAccess
                        && rightExpression2Check instanceof CtVariableAccess;

        if (instanceOfFieldAccess) {
            element.delete();
        } else if (!instanceOfFieldAccess && instanceOfVariableAccess) {
            CtField<?> field = type.getField(leftExpression2Check.toString());
            if (field != null) {
                fieldRead.setVariable(((CtVariable) field).getReference());
                leftExpression2Check.replace(fieldRead);
            } else {
                element.delete();
            }
        }
    }
}
