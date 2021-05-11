/*
Joint declarations are tricky in Spoon as they are modelede as separate elements with the same
source position. This test case tests that Sorald can find the correct local variable in a few
different configurations of joint declarations.
 */

public class JointLocalVarDeclaration {

    // the first variable in a joint declaration is a dead store
    public void firstIsDeadStore() {
        int x = 2, y = 3, z = 4; // Noncompliant
        System.out.println(y);
        System.out.println(z);
    }

    // the second variable in a joint declaration is a dead store
    public void secondIsDeadStore() {
        int x = 2, y = 3, z = 4; // Noncompliant
        System.out.println(x);
        System.out.println(z);
    }

    // the third variable in a joint declaration is a dead store
    public void thirdIsDeadStore() {
        int x = 2, y = 3, z = 4; // Noncompliant
        System.out.println(x);
        System.out.println(y);
    }
}
