package sorald.cli;

import java.util.Properties;
import picocli.CommandLine;

/** Class for providing the CLI with the current version. */
public class SoraldVersionProvider implements CommandLine.IVersionProvider {
    public static final String LOCAL_VERSION = "LOCAL";

    static final String VERSION_KEY = "Implementation-Version";
    static final String COMMIT_KEY = "Implementation-SCM-Revision";

    public static final String DEFAULT_RESOURCE_NAME = "META-INF/MANIFEST.MF";

    @Override
    public String[] getVersion() {
        return new String[] {getVersionFromPropertiesResource(DEFAULT_RESOURCE_NAME)};
    }

    /**
     * Tries to fetch the version from the given resource, that is fetched via the class loader. If
     * the version is a snapshot version, the commit hash is used instead.
     *
     * @param resourceName Name of the resource.
     * @return The resolved version.
     */
    public static String getVersionFromPropertiesResource(String resourceName) {
        Properties props = new Properties();
        try {
            props.load(
                    SoraldVersionProvider.class.getClassLoader().getResourceAsStream(resourceName));
        } catch (Exception e) {
            return LOCAL_VERSION;
        }
        return getVersionFromProperties(props);
    }

    private static String getVersionFromProperties(Properties props) {
        String version = props.getProperty(VERSION_KEY);
        String commitSha = props.getProperty(COMMIT_KEY);

        if (version == null || commitSha == null) {
            return LOCAL_VERSION;
        } else {
            return version.contains("SNAPSHOT") ? "commit: " + commitSha : version;
        }
    }
}
