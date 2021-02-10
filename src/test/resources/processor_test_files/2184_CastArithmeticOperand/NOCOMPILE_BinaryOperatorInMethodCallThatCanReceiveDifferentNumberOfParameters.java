/*
When a binary operator is in a method call that can receive different number of parameters (e.g., `void methodX(String var1, Object... var2)`),
we can get an IndexOutOfBoundsException when trying to find the type expected for the argument (i.e., the binary operator in this case).
This test checks that such an exception doesn't happen.
*/
class NOCOMPILE_BinaryOperatorInMethodCallThatCanReceiveDifferentNumberOfParameters {
    float twoThirds = 2/3; // Noncompliant; THIS LINE WAS ONLY ADDED TO MAKE THE TEST FIND A VIOLATION IN THIS FILE SO THE NEXT LINES CAN BE ACTUALLY TESTED!

    void method() {
        methodX("{} {}", 1, 1 + 1); // This is ok
        methodX("{} {} {}", 1, 2, 1 + 2); // This can cause an IndexOutOfBoundsException
    }

    static void methodX(String var1, Object var2, Object var3) {}

    static void methodX(String var1, Object... var2) {}
}
