/*
Test case to repair a single unused throwable.
 */

public class SingleUnusedThrowable {
    public void unusedThrowable() {
        new IllegalArgumentException(); // Noncompliant
    }
}