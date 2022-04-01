/*
Test the case in which the target and the argument of the call should be swapped and a null check should be removed.
 */

public class StringLiteralInsideEqualsAndNullCheckShouldBeRemoved {
    public static void main(String[] args) {
        String myString = null;

        System.out.println("Equal? " + (myString != null && myString.equals("foo"))); // Noncompliant
    }
}
