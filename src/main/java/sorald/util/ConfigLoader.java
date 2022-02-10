package sorald.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static final String CONFIG_FILE_NAME = "config.properties";
    private static final Properties properties = loadProperties();

    private ConfigLoader() {
        throw new IllegalStateException("Utility class cannot be instantiated.");
    }

    private static Properties loadProperties() {
        Properties configuration = new Properties();
        try (InputStream inputStream =
                ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME)) {
            configuration.load(inputStream);
            return configuration;
        } catch (IOException exception) {
            throw new RuntimeException(exception); // NOSONAR:S112
        }
    }

    /** Fetches URL of SonarJavaPlugin from config. */
    public static String getSonarJavaPluginUrl() {
        return properties.getProperty("SONAR_JAVA_PLUGIN_URL");
    }
}
