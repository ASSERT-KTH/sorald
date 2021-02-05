/* When the arithmetic operation is inside a method call, and an inner binary operator of the arithmetic operation
is being analyzed, we don't find it directly in the list of arguments of the method call, because that binary operator
is part of something more. Not finding it in the list of arguments causes an IndexOutOfBoundsException because
we have index -1. This test checks that that exception doesn't happen.
 */
class MultipleBinaryOperatorsInMethodCall {
    Date myDate = new Date(seconds * 1000 * 10); // Noncompliant
}
