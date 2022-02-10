package sorald.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigLoader {
    private static final String CONFIG_FILE_NAME = "config.properties";
    private static final File CONFIG_FILE =
            Paths.get("src")
                    .resolve("main")
                    .resolve("resources")
                    .resolve(CONFIG_FILE_NAME)
                    .toFile();
    private static final Properties properties = loadProperties();

    private ConfigLoader() {
        throw new IllegalStateException("Utility class cannot be instantiated.");
    }

    private static Properties loadProperties() {
        Properties configuration = new Properties();
        try (InputStream inputStream = new FileInputStream(CONFIG_FILE)) {
            configuration.load(inputStream);
            return configuration;
        } catch (IOException ignore) {
            throw new RuntimeException("Could not read config file"); // NOSONAR:S112
        }
    }

    /** Fetches URL of SonarJavaPlugin from config. */
    public static String getSonarJavaPluginUrl() {
        return properties.getProperty("SONAR_JAVA_PLUGIN_URL");
    }
}
