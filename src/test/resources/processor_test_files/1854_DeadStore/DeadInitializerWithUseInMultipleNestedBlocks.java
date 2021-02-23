/*
A dead store in an initializer, where the variable is later used in a several nested blocks along
multiple code paths. We can't simply merge the declaration with the first write, but must find
the deepest common block and put the declaration there.
*/

public class DeadInitializerWithUseInMultipleNestedBlocks {
    public int deadStoreOnInitializerWithVariableUsedInDifferentCodePaths(int a, int b) {
        int c = a; // Noncompliant

        if (a < b) {
            if (b < a) {
                if (a + b < 2) {
                    c = a + b;
                } else {
                    c = a - b;
                }
                System.out.println(c);
            } else {
                c = 2;
                System.out.println(c);
            }
        }
    }
}
