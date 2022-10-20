package sorald.cli;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import picocli.CommandLine;

/** Class for providing the CLI with the current version. */
public class SoraldVersionProvider implements CommandLine.IVersionProvider {
    public static final String LOCAL_VERSION = "LOCAL";

    static final String MAIN_CLASS = "Main-Class";
    static final String MAIN_CLASS_NAME = "sorald.Main";
    static final String VERSION_KEY = "Implementation-Version";
    static final String COMMIT_KEY = "Implementation-SCM-Revision";

    public static final String DEFAULT_RESOURCE_NAME = "META-INF/MANIFEST.MF";

    @Override
    public String[] getVersion() {
        return new String[] {getVersionFromManifests(DEFAULT_RESOURCE_NAME)};
    }

    /**
     * Tries to fetch the version from the given resource, that is fetched via the class loader. If
     * the version is a snapshot version, the commit hash is used instead.
     *
     * @param resourceName Name of the resource.
     * @return The resolved version.
     */
    public static String getVersionFromManifests(String resourceName) {
        try {
            Enumeration<URL> resources =
                    SoraldVersionProvider.class.getClassLoader().getResources(resourceName);
            while (resources.hasMoreElements()) {
                Manifest jarManifest = new Manifest(resources.nextElement().openStream());
                String mainClass = jarManifest.getMainAttributes().getValue(MAIN_CLASS);
                if (mainClass != null && mainClass.equals(MAIN_CLASS_NAME)) {
                    Attributes mainAttributes = jarManifest.getMainAttributes();
                    String version = mainAttributes.getValue(VERSION_KEY);
                    String commitSha = mainAttributes.getValue(COMMIT_KEY);

                    return version.contains("SNAPSHOT") ? "commit: " + commitSha : version;
                }
            }
        } catch (IOException e) {
            return LOCAL_VERSION;
        }
        return LOCAL_VERSION;
    }
}
