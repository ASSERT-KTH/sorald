package sorald.support;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Support class with static factory methods to create identity hash sets (i.e. hash sets that
 * operate on object identity rather than equality).
 */
public class IdentityHashSet {

    /**
     * Create an identity hash set and add all elements from the given collection to it.
     *
     * @param collection A collection.
     * @param <T> The type of elements in the hash set.
     * @return An identity hash set with all unique elements from the collection.
     */
    public static <T> Set<T> newIdentityHashSet(Collection<T> collection) {
        Set<T> set = Collections.newSetFromMap(new IdentityHashMap<>());
        set.addAll(collection);
        return set;
    }
}
