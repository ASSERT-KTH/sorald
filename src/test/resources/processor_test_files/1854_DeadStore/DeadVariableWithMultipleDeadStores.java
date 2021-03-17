/*
When a variable declaration is entirely dead AND has dead stores, we expect the variable to be
removed entirely.
 */

public class DeadVariableWithMultipleDeadStores {
    public static void main(String[] args) {
        int a = 2; // Noncompliant
        a = 3; // Noncompliant
        a = 4; // Noncompliant
        if (true) {
            System.out.println("Hello, world!");
        }
        a = 5; // Noncompliant
    }
}