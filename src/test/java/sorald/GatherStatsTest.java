package sorald;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sorald.processor.ProcessorTestHelper;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GatherStatsTest {
    @Test
    public void sorald_createsStatsOutputFile_whenSpecifiedFileDoesNotExist(@TempDir File tempDir)
            throws Exception {
        ProcessorTestHelper.ProcessorTestCase<?> testCase =
                ProcessorTestHelper.getTestCaseStream().findFirst().get();
        File expectedStatsFile = tempDir.toPath().resolve("stats.json").toFile();

        ProcessorTestHelper.runSorald(
                testCase,
                Constants.ARG_SYMBOL + Constants.ARG_STATS_OUTPUT_FILE,
                expectedStatsFile.getAbsolutePath());

        assertTrue(expectedStatsFile.isFile());
    }
}
