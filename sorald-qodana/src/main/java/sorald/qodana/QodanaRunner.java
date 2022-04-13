package sorald.qodana;

import com.contrastsecurity.sarif.Result;
import com.contrastsecurity.sarif.SarifSchema210;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.api.model.WaitResponse;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.google.common.flogger.FluentLogger;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apache.commons.io.FileUtils;

class QodanaRunner {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private String resultFolder;
    private String cacheFolder;
    private String qodanaImageName;
    private String resultPathString;
    private boolean removeResultDir = true;
    private String sourceFileRoot;

    private QodanaRunner(Builder builder) {
        this.resultFolder = builder.resultFolder;
        this.cacheFolder = builder.cacheFolder;
        this.qodanaImageName = builder.qodanaImageName;
        this.resultPathString = builder.resultPathString;
        this.removeResultDir = builder.removeResultDir;
        this.sourceFileRoot = builder.sourceFileRoot;
    }

    public List<Result> runQodana(Path sourceRoot) {
        sourceRoot = fixWindowsPath(sourceRoot);
        logger.atInfo().log("Running Qodana on %s", sourceRoot);
        copyQodanaRules(sourceRoot);
        DockerClientConfig standard =
                DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerHttpClient httpClient = createHttpClient(standard);
        try (DockerClient dockerClient = DockerClientImpl.getInstance(standard, httpClient); ) {
            Path resultPath = Path.of(resultPathString);
            if (resultPath.toFile().exists()) {
                return parseSarif(resultPath);
            }
            Optional<Image> qodana = findQodanaImage(dockerClient);
            if (qodana.isPresent()) {
                return executeQodana(sourceRoot, dockerClient, qodana);
            }
        } catch (Exception e) {
            logger.atSevere().withCause(e).log("Error running qodana");
        }
        return List.of();
    }

    private List<Result> executeQodana(
            Path sourceRoot, DockerClient dockerClient, Optional<Image> qodana)
            throws InterruptedException, IOException {
        HostConfig hostConfig = createHostConfig(sourceRoot);
        CreateContainerResponse container =
                createQodanaContainer(dockerClient, qodana.get(), hostConfig);
        List<Result> results = startQodanaContainer(dockerClient, container);
        cleanCaches(sourceRoot);
        return results;
    }

    private void cleanCaches(Path sourceRoot) throws IOException {
        if (removeResultDir) {
            FileUtils.deleteDirectory(stringToFile(resultFolder));
        }
        FileUtils.deleteDirectory(stringToFile(cacheFolder));
        Files.deleteIfExists(Path.of(sourceRoot.toString(), "qodana.yaml"));
    }

    /**
     * Converts the given path as string to a file object
     *
     * @param path the path as string
     * @return the file object
     */
    private File stringToFile(String path) {
        return Path.of(path).toFile();
    }

    private void copyQodanaRules(Path sourceRoot) {
        try {
            File qodanaRules = new File(this.getClass().getResource("/qodana.yml").toURI());
            File copyQodanaRules = new File(sourceRoot.toString(), "qodana.yml");
            Files.writeString(
                    copyQodanaRules.toPath(),
                    Files.readString(qodanaRules.toPath()),
                    StandardOpenOption.CREATE);
        } catch (URISyntaxException | IOException e) {
            logger.atSevere().withCause(e).log("Could not write qodana.yaml");
        }
    }

    private List<Result> startQodanaContainer(
            DockerClient dockerClient, CreateContainerResponse container)
            throws InterruptedException {
        dockerClient.startContainerCmd(container.getId()).exec();
        WaitContainerResultCallback exec =
                dockerClient
                        .waitContainerCmd(container.getId())
                        .exec(new WaitContainerResultCallback());
        List<Result> results = new ArrayList<>();
        dockerClient
                .waitContainerCmd(container.getId())
                .exec(
                        new ResultCallbackTemplate<WaitContainerResultCallback, WaitResponse>() {
                            @Override
                            public void onNext(WaitResponse object) {
                                try {
                                    exec.awaitCompletion();
                                    results.addAll(parseSarif(Path.of(resultPathString)));
                                } catch (IOException | InterruptedException e) {
                                    logger.atSevere().withCause(e).log("Could not parse sarif");
                                }
                            }
                        })
                .awaitCompletion();
        return results;
    }

    private CreateContainerResponse createQodanaContainer(
            DockerClient dockerClient, Image qodana, HostConfig hostConfig) {
        return dockerClient
                .createContainerCmd(qodana.getId())
                .withHostConfig(hostConfig)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .withCmd("-d", sourceFileRoot)
                .exec();
    }

    private HostConfig createHostConfig(Path sourceRoot) {
        Volume sourceFile = new Volume("/data/project/");
        Volume targetFile = new Volume("/data/results/");
        Volume cacheDir = new Volume("/data/cache/");
        Bind bind = new Bind(sourceRoot.toFile().getAbsolutePath(), sourceFile);
        Bind resultsBind = new Bind(new File(resultFolder).getAbsolutePath(), targetFile);
        Bind cacheBind = new Bind(new File(cacheFolder).getAbsolutePath(), cacheDir);
        return HostConfig.newHostConfig()
                .withBinds(bind, cacheBind, resultsBind); // .withAutoRemove(true);
    }

    private Optional<Image> findQodanaImage(DockerClient dockerClient) {
        List<Image> images = dockerClient.listImagesCmd().exec();
        return images.stream()
                .filter(v -> Arrays.asList(v.getRepoTags()).contains(qodanaImageName))
                .findFirst();
    }

    private ApacheDockerHttpClient createHttpClient(DockerClientConfig standard) {
        return new ApacheDockerHttpClient.Builder()
                .dockerHost(standard.getDockerHost())
                .sslConfig(standard.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();
    }

    private Path fixWindowsPath(Path sourceRoot) {
        if (sourceRoot
                .toFile()
                .toPath()
                .toString()
                .endsWith(Path.of("/src/main/java").toString())) {
            if (sourceRoot.getRoot() == null) {
                sourceRoot = sourceRoot.subpath(0, sourceRoot.getNameCount() - 3);
            } else {
                sourceRoot =
                        Paths.get(
                                sourceRoot.getRoot().toString(),
                                sourceRoot.subpath(0, sourceRoot.getNameCount() - 3).toString());
            }
        }
        return sourceRoot;
    }

    private List<Result> parseSarif(Path resultPath) throws IOException {
        StringReader reader = new StringReader(Files.readString(resultPath));
        ObjectMapper mapper = new ObjectMapper();
        SarifSchema210 sarif = mapper.readValue(reader, SarifSchema210.class);
        return sarif.getRuns().get(0).getResults();
    }

    static class Builder {

        private String resultFolder = "./.results/";
        private String cacheFolder = "./.laughing/";
        private String qodanaImageName = "jetbrains/qodana-jvm-community:2021.3";
        private String resultPathString = resultFolder + "qodana.sarif.json";
        private boolean removeResultDir = true;
        private String sourceFileRoot = "./src/main/java";

        public Builder withResultFolder(String resultFolder) {
            this.resultFolder = resultFolder;
            this.resultPathString = resultFolder + "qodana.sarif.json";
            return this;
        }

        public Builder withCacheFolder(String cacheFolder) {
            this.cacheFolder = cacheFolder;
            return this;
        }

        public Builder withQodanaImageName(String qodanaImageName) {
            this.qodanaImageName = qodanaImageName;
            return this;
        }

        public Builder withRemoveResultDir(boolean removeResultDir) {
            this.removeResultDir = removeResultDir;
            return this;
        }

        public Builder withSourceFileRoot(String sourceFileRoot) {
            this.sourceFileRoot = sourceFileRoot;
            return this;
        }

        public QodanaRunner build() {
            return new QodanaRunner(this);
        }
    }
}
