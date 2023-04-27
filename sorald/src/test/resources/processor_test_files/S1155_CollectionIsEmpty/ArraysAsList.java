import java.util.Arrays;

public class ArraysAsList {
    void main() {
        if (Arrays.asList("a", "b", "c").size() == 0) { // Noncompliant
            return;
        }
        if (Arrays.asList("a", "b", "c").size() < 1) { // Noncompliant
            return;
        }
        if (Arrays.asList("a", "b", "c").size() <= 0) { // Noncompliant
            return;
        }
        if (Arrays.asList("a", "b", "c").size() > 3) { // Compliant
            return;
        }
        if (Arrays.asList("a", "b", "c").size() >= 1) { // Noncompliant
            return;
        }
        if (0 >= Arrays.asList("a", "b", "c").size()) { // Noncompliant
            return;
        }
        if (Arrays.asList(1, 2, 3).size() >= 0) { // Compliant
            return;
        }
        if (1 < Arrays.asList("a", "b", "c").size()) { // Compliant
            return;
        }
    }
}
