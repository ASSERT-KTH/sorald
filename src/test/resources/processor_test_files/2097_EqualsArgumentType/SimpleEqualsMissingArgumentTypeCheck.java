/*
Test case for verifying that the processor can correctly add both a type check and null check
at the start of the method.
 */

public class SimpleEqualsMissingArgumentTypeCheck {
    private final int x;

    public SimpleEqualsMissingArgumentTypeCheck(int x) {
        this.x = x;
    }

    @Override
    public boolean equals(Object obj) { // Noncompliant; missing argument type test
        return this.x == ((SimpleEqualsMissingArgumentTypeCheck) obj).x;
    }
}