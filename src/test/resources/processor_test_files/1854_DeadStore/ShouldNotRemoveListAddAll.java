/*
List.addAll() has a side effect, and so it should not be removed when identified as a dead store.
 */

public class ShouldNotRemoveListAddAll {
    public List<Integer> concatenate(List<Integer> lhs, List<Integer> rhs) {
        List<Integer> base = new ArrayList<>(lsh);
        boolean changed = base.addAll(rhs); // Noncompliant
        return base;
    }
}