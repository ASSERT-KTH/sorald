import java.io.Serializable;

public class UnusedPrivateField implements Serializable {
    private int a = 42; // Noncompliant
    private static String b = "Hello world!"; // Noncompliant
    private static final long serialVersionUID = 42L; // Compliant as this field is used in deserialization
    protected double c = 3.14; // Compliant as this field may be used in subclasses
    private static int used = 1, unused = 2; // Compliant and noncompliant respectively

    public int compute(int number) {
        return number * used;
    }
}
