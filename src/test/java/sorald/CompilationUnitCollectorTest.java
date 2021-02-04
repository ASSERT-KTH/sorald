package sorald;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static sorald.support.IdentityHashSet.newIdentityHashSet;

import java.util.Collection;
import org.junit.jupiter.api.Test;
import sorald.processor.ProcessorTestHelper;
import spoon.Launcher;
import spoon.reflect.declaration.CtType;

class CompilationUnitCollectorTest {

    /**
     * Test that the UniqueTypesCollector can collect and retain many unique compilation units
     * without any problems.
     */
    @Test
    public void collect_canCollectManyCompilationUnits() {
        // arrange
        Launcher launcher = new Launcher();
        String originalFilesPath = ProcessorTestHelper.TEST_FILES_ROOT.toAbsolutePath().toString();
        var config = new SoraldConfig();
        config.setOriginalFilesPath(originalFilesPath);
        CompilationUnitCollector.reset(config);

        launcher.addInputResource(originalFilesPath);
        Collection<CtType<?>> types = launcher.buildModel().getAllTypes();
        var expectedCUs = launcher.getFactory().CompilationUnit().getMap().values();

        // act
        types.forEach(type -> CompilationUnitCollector.getInstance().collect(type));

        // assert
        var collectedCUs = CompilationUnitCollector.getInstance().getCollectedCompilationUnits();
        assertThat(newIdentityHashSet(collectedCUs), equalTo(newIdentityHashSet(expectedCUs)));
    }
}
