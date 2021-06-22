/*
We should only set the interrupted flag if an interrupt actually occurred, so when a catch has
multiple types in the catch clause, we need to do a type check.
 */
import java.util.concurrent.ExecutionException;

public class CatchMultipleTypes {
    public static void method() {
        try {
            if (1 < 2) {
                throw new ExecutionException(new RuntimeException());
            } else {
                throw new InterruptedException();
            }
        } catch (InterruptedException | ExecutionException e) { // Noncompliant
            throw new RuntimeException(e);
        }
    }
}