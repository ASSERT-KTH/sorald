/*
Test with an unused local variable declared in a for loop header.
 */

public class UnusedVariableInForLoopHeader {
    public static void main(String[] args) {
        for (int x = 0, y = 0, z = 10; x <= z; x++) { // Noncompliant, y is not used
            System.out.println("Current: " + x);
            System.out.println("Goal: " + z);
        }
    }
}