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
