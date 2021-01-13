public class LiteralIntOperand {
    public static void literalIntAsLeftOperand() {
        int a = 22;
        long longVal = 1000 + a; // Noncompliant
        float floatVal = 1000 + a; // Noncompliant
        double doubleVal = 1000 + a; // Noncompliant
        double doubleDiv = 1000 / a; // Noncompliant
    }

    public static void literalIntAsRightOperand() {
        int a = 22;
        long longVal = a + 1000; // Noncompliant
        float floatVal = a + 1000; // Noncompliant
        double doubleVal = a + 1000; // Noncompliant
        double doubleDiv = a / 1000; // Noncompliant
    }
}