This repair adds a type test to any `equals(Object)` method that lacks such.

Example:

```diff
    @Override
    public boolean equals(Object obj) {
+       if (obj == null || getClass() != obj.getClass()) {
+               return false;
+       }
        return toFile().equals(((SpoonResource) obj).toFile());
    }
```
