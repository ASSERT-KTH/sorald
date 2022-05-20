/*
Test case with unary prefix/suffix operators in statement position.
 */

public class UnaryOpsInStatementPosition {
    public static void main(String[] args) {
        int x = 2;
        System.out.println(x);

        x++; // Noncompliant
        x = 3;
        System.out.println(x);

        ++x; // Noncompliant
        x = 4;
        System.out.println(x);

        x--; // Noncompliant
        x = 5;
        System.out.println(x);

        --x; // Noncompliant
        x = 6;
        System.out.println(x);
    }
}