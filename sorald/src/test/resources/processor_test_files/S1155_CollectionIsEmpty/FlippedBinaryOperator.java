import java.util.Collection;

public class FlippedBinaryOperator {
    public static void main(String[] args) {
        Collection myCollection = null;

        if (0 == myCollection.size()) {  // Noncompliant
            /* ... */
        }
        if (0 >= myCollection.size()) {  // Noncompliant
            /* ... */
        }
        if (3 >= myCollection.size()) { // Compliant
            /* ... */
        }
        if (1 <= myCollection.size()) {  // Noncompliant
            /* ... */
        }
    }
}
