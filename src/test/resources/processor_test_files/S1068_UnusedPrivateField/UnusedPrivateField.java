import java.io.Serializable;

public class UnusedPrivateField implements Serializable {
    private static final long serialVersionUID = 42L; // Compliant as this field is used in deserialization

    private int a = 42; // Noncompliant
    private static String b = "Hello world!"; // Noncompliant
    protected double c = 3.14; // Compliant as this field may be used in subclasses
}
