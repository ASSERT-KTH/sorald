/*
A dead store that is directly succeded by another assignment. We want to "merge" the declaration
with the non-dead store.
 */

public class DeadInitializerInFlatBlock {
    public int deadStoreOnInitializer() {
        int a = 2; // Noncompliant
        a = 3;
        return a;
    }
}