public class VariableIntOperand {
    public static void method() {
        int a = 22;
        int b = 33;
        long longVal = a + b; // Noncompliant
        float floatVal = a + b; // Noncompliant
        double doubleVal = a + b; // Noncompliant
        double doubleDiv = a / b; // Noncompliant
    }
}
