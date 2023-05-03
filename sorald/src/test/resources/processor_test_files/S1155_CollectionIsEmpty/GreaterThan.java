import java.util.Collection;

public class GreaterThan {
    public static void main(String[] args) {
        Collection myCollection = null;

        if (myCollection.size() >= 1) {  // Noncompliant
            /* ... */
        }
        if (myCollection.size() > 3) { // Compliant
            /* ... */
        }
        if (myCollection.size() >= 1) {  // Noncompliant
            /* ... */
        } else if (myCollection.size() > 0) {  // Noncompliant
            /* ... */
        }
    }
}
