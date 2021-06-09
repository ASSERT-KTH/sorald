/*
Test the simple case: the target and the argument of the call should be swapped.
 */

public class StringLiteralInsideEqualsSimpleCase {
    public static void main(String[] args) {
        String myString = null;

        System.out.println("Equal? " + myString.equals("foo")); // Noncompliant; will raise a NPE
    }
}
