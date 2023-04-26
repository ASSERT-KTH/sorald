import java.util.Collection;

public class LessThan {
    public static void main(String[] args) {
        Collection myCollection = null;

        if (myCollection.size() < 1) {  // Noncompliant
            /* ... */
        }
    }
}
