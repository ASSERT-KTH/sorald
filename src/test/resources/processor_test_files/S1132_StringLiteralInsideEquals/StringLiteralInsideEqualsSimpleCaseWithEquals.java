/*
Test the simple case with equals(java.lang.Object): the target and the argument of the call should be swapped.
 */

public class StringLiteralInsideEqualsSimpleCaseWithEquals {
    public static void main(String[] args) {
        String myString = null;

        System.out.println("Equal? " + myString.equals("foo")); // Noncompliant; will raise a NPE
    }
}
