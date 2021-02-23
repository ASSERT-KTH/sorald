/*
A dead store in an initializer, where the variable is later used in several nested blocks along
multiple code paths. A key detail here is that there is a "root write" at a common parent of
all the code paths in which the variable is used, and we want to merge the declaration with that
write.
 */

public class DeadInitializerWithRootWriteInNestedBlock {
    public void deadStoreOnInitializerWithVariableUsedInDifferentCodePaths2(int a, int b) {
        int c = a; // Noncompliant

        if (a < b) {
            c = 22;
            if (b < a) {
                if (a + b < 2) {
                    c = a + b;
                } else {
                    c = a - b;
                }
                System.out.println(c);
            } else {
                System.out.println(c);
            }
        }
    }
}