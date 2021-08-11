Objects which are pooled, such as Strings or boxed primitives, and potentially reused should not be used for synchronization, since they can cause deadlocks. The transformation will do the following. If the lock is a field of the current class where the synchronization block is in, then it will simply add a new field as an `Object` lock. If the lock is obtained from another object through the `get` method, it will add a new field for the new `Object` lock and a new method to get the object.

Example:
```diff
   private final Boolean bLock = Boolean.FALSE;
+  private final Object bLockLegal = new Object();
   private final InnerClass i = new InnerClass();
-  void method1() {
-    synchronized(bLock) {}
-    synchronized(i.getLock()){}
-  }
+  void method1() {
+    synchronized(bLockLegal) {}
+    synchronized(i.getLockLegal()){}
+  }
   class InnerClass {
        public Boolean innerLock = Boolean.FALSE;
+       public Object innerLockLegal = new Object();

        public Boolean getLock() {
            return this.innerLock;
        }
+       public Object getLockLegal() {
+           return this.innerLockLegal;
+       }
  }
```
