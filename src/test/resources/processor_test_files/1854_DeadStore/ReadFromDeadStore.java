/*
When a dead store is in expression position (e.g. post-increment or an expression assignment),
it's always read from. Thus, deleting the dead store is inappropriate; it should be replaced
with a variable read _or_ binary operator instead.
 */

public class ReadFromDeadStore {

    void postIncrement() {
        int x = 10;
        int y = x++; // Noncompliant
        System.out.println(y);
    }

    void expressionAssignment() {
        String line = null;
        while ((line = readLine()) != null) { // Noncompliant
        }
    }

    static String readLine() {
        return "hello!";
    }
}