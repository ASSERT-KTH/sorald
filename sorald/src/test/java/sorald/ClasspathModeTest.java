package sorald;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import sorald.event.StatsMetadataKeys;
import sorald.processor.CastArithmeticOperandProcessor;
import sorald.util.MavenUtils;

import spoon.MavenLauncher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/** Tests for running Sorald in classpath mode. */
class ClasspathModeTest {

    @Test
    void resolveClasspathFrom_enablesRepairOfViolation_thatRequiresClasspathToDetect(
            @TempDir File workdir) throws IOException, XmlPullParserException {
        // arrange
        org.apache.commons.io.FileUtils.copyDirectory(
                TestHelper.PATH_TO_RESOURCES_FOLDER
                        .resolve("scenario_test_files")
                        .resolve("classpath-dependent-project")
                        .toFile(),
                workdir);

        Path statsFile = workdir.toPath().resolve("stats.json");
        Path source = workdir.toPath().resolve("src").resolve("main").resolve("java");

        assertThat(Files.exists(source), equalTo(true));

        String castArithmOperandKey = new CastArithmeticOperandProcessor().getRuleKey();
        String[] args = {
            Constants.REPAIR_COMMAND_NAME,
            Constants.ARG_SOURCE,
            source.toString(),
            Constants.ARG_RULE_KEY,
            castArithmOperandKey,
            Constants.ARG_STATS_OUTPUT_FILE,
            statsFile.toString(),
            Constants.ARG_RESOLVE_CLASSPATH_FROM,
            workdir.getAbsolutePath()
        };

        System.out.println(
                "Classpath " + MavenUtils.resolveClasspath(Path.of(workdir.getAbsolutePath())));

        MavenLauncher launcher =
                new MavenLauncher(workdir.getAbsolutePath(), MavenLauncher.SOURCE_TYPE.ALL_SOURCE);
        System.out.println(
                "Classpath " + Arrays.toString(launcher.getEnvironment().getSourceClasspath()));

        // act
        Main.main(args);

        // assert
        System.out.println(Files.readString(statsFile));
        JSONObject stats = FileUtils.readJSON(statsFile);
        JSONArray repairs = stats.getJSONArray(StatsMetadataKeys.REPAIRS);
        assertThat(repairs.length(), equalTo(1));
        JSONObject repair = repairs.getJSONObject(0);
        assertThat(
                repair.getString(StatsMetadataKeys.REPAIR_RULE_KEY), equalTo(castArithmOperandKey));
    }
}
