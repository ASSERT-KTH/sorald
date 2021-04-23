/*
 * Test to verify that the processor does not remove method invocations by default,
 * as these may have side effects.
 */

public class ShouldNotRemoveInvocations {
    public void methodWithDeadStoreMethodCall() {
        int a = someMethod(); // Noncompliant
        a = otherMethod(); // Noncompliant
        a = 2;
        System.out.println(a);
    }
}