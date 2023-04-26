import java.util.Collection;

public class EqualTo {
    public static void main(String[] args) {
        Collection myCollection = null;

        if (myCollection.size() == 0) {  // Noncompliant
            /* ... */
        }
        if (myCollection.size() != 0) {  // Noncompliant
            /* ... */
        }
    }
}
