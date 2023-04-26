Using `Collection.size()` to test for emptiness works, but using `Collection.isEmpty()` makes the code more readable and
can be more performant. Expressions `myCollection.size() == 0` are replaced by `myCollection.isEmpty()`, and
expressions `myCollection.size() != 0` are replaced by `!myCollection.isEmpty()`.

Example:
```diff
- if (myCollection.size() == 0) {  // Noncompliant
+ if (myCollection.isEmpty()) {
...
- if (myCollection.size() != 0) {  // Noncompliant
+ if (!myCollection.isEmpty()) {
...
- if (myCollection.size() < 1) {  // Noncompliant
+ if (myCollection.isEmpty()) {
```

> We ignore expressions such as `0 == myCollections.size()` based on heuristics
> that it is not a common practice to do so.
