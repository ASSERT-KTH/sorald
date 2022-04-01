package sorald.cli;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

public class SoraldVersionProviderTest {
    private static final Path BOGUS_MANIFESTS = Paths.get("META-INF").resolve("bogus-manifests");

    private static final String VERSION_IN_MANIFESTS = "1.2.3";
    private static final String COMMIT_IN_MANIFESTS = "123456";

    @Test
    public void getVersionFromPropertiesResource_returnsLocalVersion_whenResourceDoesNotExist() {
        assertThat(
                SoraldVersionProvider.getVersionFromPropertiesResource("no/such/resource"),
                equalTo(SoraldVersionProvider.LOCAL_VERSION));
    }

    @Test
    public void getVersionFromPropertiesResource_returnsVersion_whenNonSnapshot() {
        String resourceName = BOGUS_MANIFESTS.resolve("MANIFEST-RELEASE-VERSION.MF").toString();
        assertThat(
                SoraldVersionProvider.getVersionFromPropertiesResource(resourceName),
                equalTo(VERSION_IN_MANIFESTS));
    }

    @Test
    public void getVersionFromPropertiesResource_returnsCommitSha_whenSnapshot() {
        String resourceName = BOGUS_MANIFESTS.resolve("MANIFEST-SNAPSHOT-VERSION.MF").toString();
        assertThat(
                SoraldVersionProvider.getVersionFromPropertiesResource(resourceName),
                equalTo("commit: " + COMMIT_IN_MANIFESTS));
    }

    @Test
    public void
            getVersionFromPropertiesResource_returnsLocalVersion_whenResourceIsMissingVersion() {
        String resourceName = BOGUS_MANIFESTS.resolve("MANIFEST-WITHOUT-VERSION.MF").toString();
        assertThat(
                SoraldVersionProvider.getVersionFromPropertiesResource(resourceName),
                equalTo(SoraldVersionProvider.LOCAL_VERSION));
    }
}
