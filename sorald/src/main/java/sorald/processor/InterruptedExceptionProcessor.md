A `catch` block that catches an `InterruptedException`, but neither re-interrupts the method nor rethrows the `InterruptedException`, i.e., ignores the `InterruptedException`, is augmented with `Thread.currentThread().interrupt();`.

Example:
```diff
    public void run() {
        try {
            while (true) {
                // do stuff
            }
-       } catch (InterruptedException e) { // Noncompliant; logging is not enough
+       } catch (InterruptedException e) {
            LOGGER.log(Level.WARN, "Interrupted!", e);
+           Thread.currentThread().interrupt();
        }
    }
```

Sorald places the interrupt as late as possible in the catch block, but before any throws or returns.

If there are multiple exceptions handled in a single catch block, a new catch block is added.

Example:

```diff
public static void method() {
    try {
        if (1 < 2) {
            throw new ExecutionException(new RuntimeException());
        } else {
            throw new InterruptedException();
        }
    }
    // Noncompliant
-   catch (InterruptedException | ExecutionException e) {
+   catch (ExecutionException e) {
        throw new RuntimeException(e);
    }
+   catch (InterruptedException e) {
+       Thread.currentThread().interrupt();
+       throw new RuntimeException(e);
+   }
}
```
