package sorald.cli;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarException;

class SoraldVersionProviderTest {
    private static final Path BOGUS_MANIFESTS = Paths.get("META-INF").resolve("bogus-manifests");

    private static final String VERSION_IN_MANIFESTS = "1.2.3";
    private static final String COMMIT_IN_MANIFESTS = "123456";

    @Test
    void getVersionFromPropertiesResource_returnsLocalVersion_whenResourceDoesNotExist()
            throws JarException {
        assertThat(
                SoraldVersionProvider.getVersionFromManifests("no/such/resource"),
                equalTo(SoraldVersionProvider.LOCAL_VERSION));
    }

    @Test
    void getVersionFromPropertiesResource_returnsVersion_whenNonSnapshot() throws JarException {
        String resourceName = BOGUS_MANIFESTS.resolve("MANIFEST-RELEASE-VERSION.MF").toString();
        assertThat(
                SoraldVersionProvider.getVersionFromManifests(resourceName),
                equalTo(VERSION_IN_MANIFESTS));
    }

    @Test
    void getVersionFromPropertiesResource_returnsCommitSha_whenSnapshot() throws JarException {
        String resourceName = BOGUS_MANIFESTS.resolve("MANIFEST-SNAPSHOT-VERSION.MF").toString();
        assertThat(
                SoraldVersionProvider.getVersionFromManifests(resourceName),
                equalTo("commit: " + COMMIT_IN_MANIFESTS));
    }

    @Test
    void getVersionFromPropertiesResource_throwsJarException_whenResourceIsMissingVersion() {
        String resourceName = BOGUS_MANIFESTS.resolve("MANIFEST-WITHOUT-VERSION.MF").toString();
        assertThrows(
                JarException.class,
                () -> SoraldVersionProvider.getVersionFromManifests(resourceName));
    }

    @Test
    void getVersionFromPropertiesResource_throwsJarException_whenResourceIsMissingCommitSha() {
        String resourceName = BOGUS_MANIFESTS.resolve("MANIFEST-WITHOUT-COMMIT-SHA.MF").toString();
        assertThrows(
                JarException.class,
                () -> SoraldVersionProvider.getVersionFromManifests(resourceName));
    }
}
