Sorald fixes the violations of this rule by replacing each invocation of `Thread.run()` with an invocation of `Thread.start()`.

Example:
```diff
    Thread myThread = new Thread(runnable);
-   myThread.run();
+   myThread.start();
```
