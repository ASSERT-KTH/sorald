/*
As a CtInvocation is a CtStatement, and the DeadStoreProcessor processes CtStatement,
it is important to test cases of dead stores with CtInvocation's as the assigned expr.
See #412 for a related bug.
 */

public class DeadStoreWithInvocation {
    public int deadInitializer() {
        int a = Integer.parseInt("1"); // Noncompliant
        a = 2;
        return a;
    }

    public void deadAssignment() {
        int a = 2;
        System.out.println(a);
        a = Integer.parseInt("1"); // Noncompliant
    }
}