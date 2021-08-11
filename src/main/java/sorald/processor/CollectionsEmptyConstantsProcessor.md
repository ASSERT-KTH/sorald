The `EMPTY_...` fields from `Collections` return raw types, so they are replaced by the `empty...()` methods that return generic ones.

Example:
```diff
- List<String> collection1 = Collections.EMPTY_LIST;  // Noncompliant
- Map<String, String> collection2 = Collections.EMPTY_MAP;  // Noncompliant
- Set<String> collection3 = Collections.EMPTY_SET;  // Noncompliant
+ List<String> collection1 = Collections.emptyList();
+ Map<String, String> collection2 = Collections.emptyMap();
+ Set<String> collection3 = Collections.emptySet();
```
