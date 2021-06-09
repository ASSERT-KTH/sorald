/*
Test the case in which the target and the argument of the call should be swapped and a null check should be removed.
The null check is, in this case, as a parent binary operator.
 */

public class StringLiteralInsideEqualsAndNullCheckShouldBeRemovedWhenItIsAsParent {
    public static void main(String[] args) {
        String myString = null;

        System.out.println("Equal? " + (myString != null && (myString != "AAA" && myString.equals("foo")))); // Noncompliant
    }
}
