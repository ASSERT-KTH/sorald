The repair consists of add a serialVersionUID to classes implementing Serializable
Example:
```diff
public class NoUID implements Serializable {
+  private static final long serialVersionUID = 1L;
}
```
