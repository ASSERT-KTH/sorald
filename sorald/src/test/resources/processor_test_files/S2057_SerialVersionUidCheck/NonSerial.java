import java.io.Serializable;
public class NonSerial {
    public static class Serial implements Serializable { // Noncompliant
    }
}