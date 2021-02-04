package sorald;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import org.junit.jupiter.api.Test;
import sorald.processor.ProcessorTestHelper;
import spoon.Launcher;
import spoon.reflect.declaration.CtType;

class UniqueTypesCollectorTest {

    /**
     * Test that the UniqueTypesCollector can collect and retain many unique types without any
     * problems.
     */
    @Test
    public void canCollectManyTypes() {
        // arrange
        Launcher launcher = new Launcher();
        launcher.addInputResource(ProcessorTestHelper.TEST_FILES_ROOT.toAbsolutePath().toString());
        Collection<CtType<?>> types = launcher.buildModel().getAllTypes();

        // act
        types.forEach(type -> UniqueTypesCollector.getInstance().collect(type));

        // assert
        var collectedTypes = UniqueTypesCollector.getInstance().getTopLevelTypes4Output().values();
        assertThat(identityHashSet(collectedTypes), equalTo(identityHashSet(types)));
    }

    /** Turn the collection into an identity hash set */
    private static <T> Set<T> identityHashSet(Collection<T> collection) {
        Set<T> idHashSet = Collections.newSetFromMap(new IdentityHashMap<>());
        idHashSet.addAll(collection);
        return idHashSet;
    }
}
