package spoon;


public class MavenLauncher extends spoon.Launcher {
    private java.lang.String m2RepositoryPath;

    private spoon.MavenLauncher.SOURCE_TYPE sourceType;

    public enum SOURCE_TYPE {
        APP_SOURCE, TEST_SOURCE, ALL_SOURCE;}

    public MavenLauncher(java.lang.String mavenProject, spoon.MavenLauncher.SOURCE_TYPE sourceType) {
        this(mavenProject, java.nio.file.Paths.get(java.lang.System.getProperty("user.home"), ".m2", "repository").toString(), sourceType);
    }

    public MavenLauncher(java.lang.String mavenProject, java.lang.String m2RepositoryPath, spoon.MavenLauncher.SOURCE_TYPE sourceType) {
        super();
        this.m2RepositoryPath = m2RepositoryPath;
        this.sourceType = sourceType;
        java.io.File mavenProjectFile = new java.io.File(mavenProject);
        if (!(mavenProjectFile.exists())) {
            throw new spoon.SpoonException((mavenProject + " does not exist."));
        }
        spoon.MavenLauncher.InheritanceModel model;
        try {
            model = readPOM(mavenProject, null);
        } catch (java.lang.Exception e) {
            throw new spoon.SpoonException("Unable to read the pom", e);
        }
        if (model == null) {
            throw new spoon.SpoonException("Unable to create the model, pom not found?");
        }
        if (((spoon.MavenLauncher.SOURCE_TYPE.APP_SOURCE) == sourceType) || ((spoon.MavenLauncher.SOURCE_TYPE.ALL_SOURCE) == sourceType)) {
            java.util.List<java.io.File> sourceDirectories = model.getSourceDirectories();
            for (java.io.File sourceDirectory : sourceDirectories) {
                this.addInputResource(sourceDirectory.getAbsolutePath());
            }
        }
        if (((spoon.MavenLauncher.SOURCE_TYPE.TEST_SOURCE) == sourceType) || ((spoon.MavenLauncher.SOURCE_TYPE.ALL_SOURCE) == sourceType)) {
            java.util.List<java.io.File> testSourceDirectories = model.getTestDirectories();
            for (java.io.File sourceDirectory : testSourceDirectories) {
                this.addInputResource(sourceDirectory.getAbsolutePath());
            }
        }
        java.util.List<java.io.File> dependencies = model.getDependencies(false);
        java.lang.String[] classpath = new java.lang.String[dependencies.size()];
        for (int i = 0; i < (dependencies.size()); i++) {
            java.io.File file = dependencies.get(i);
            classpath[i] = file.getAbsolutePath();
        }
        this.getModelBuilder().setSourceClasspath(classpath);
        this.getEnvironment().setComplianceLevel(model.getSourceVersion());
    }

    private spoon.MavenLauncher.InheritanceModel readPOM(java.lang.String path, spoon.MavenLauncher.InheritanceModel parent) throws java.io.IOException, org.codehaus.plexus.util.xml.pull.XmlPullParserException {
        if ((!(path.endsWith(".xml"))) && (!(path.endsWith(".pom")))) {
            path = java.nio.file.Paths.get(path, "pom.xml").toString();
        }
        java.io.File pomFile = new java.io.File(path);
        if (!(pomFile.exists())) {
            return null;
        }
        org.apache.maven.model.io.xpp3.MavenXpp3Reader pomReader = new org.apache.maven.model.io.xpp3.MavenXpp3Reader();
        try (java.io.FileReader reader = new java.io.FileReader(pomFile)) {
            org.apache.maven.model.Model model = pomReader.read(reader);
            spoon.MavenLauncher.InheritanceModel inheritanceModel = new spoon.MavenLauncher.InheritanceModel(model, parent, pomFile.getParentFile());
            for (java.lang.String module : model.getModules()) {
                inheritanceModel.addModule(readPOM(java.nio.file.Paths.get(pomFile.getParent(), module).toString(), inheritanceModel));
            }
            return inheritanceModel;
        }
    }

    class InheritanceModel {
        private java.util.List<spoon.MavenLauncher.InheritanceModel> modules = new java.util.ArrayList<>();

        private org.apache.maven.model.Model model;

        private spoon.MavenLauncher.InheritanceModel parent;

        private java.io.File directory;

        InheritanceModel(org.apache.maven.model.Model model, spoon.MavenLauncher.InheritanceModel parent, java.io.File directory) {
            this.model = model;
            this.parent = parent;
            this.directory = directory;
            if ((parent == null) && ((model.getParent()) != null)) {
                try {
                    java.io.File parentPath = new java.io.File(directory, model.getParent().getRelativePath());
                    this.parent = readPOM(parentPath.getPath(), null);
                } catch (java.lang.Exception e) {
                    spoon.Launcher.LOGGER.debug(("Parent model cannot be resolved: " + (e.getMessage())));
                }
            }
        }

        public void addModule(spoon.MavenLauncher.InheritanceModel module) {
            modules.add(module);
        }

        public org.apache.maven.model.Model getModel() {
            return model;
        }

        public spoon.MavenLauncher.InheritanceModel getParent() {
            return parent;
        }

        public java.util.List<java.io.File> getSourceDirectories() {
            java.util.List<java.io.File> output = new java.util.ArrayList<>();
            java.lang.String sourcePath = null;
            org.apache.maven.model.Build build = model.getBuild();
            if (build != null) {
                sourcePath = build.getSourceDirectory();
            }
            if (sourcePath == null) {
                sourcePath = java.nio.file.Paths.get(directory.getAbsolutePath(), "src", "main", "java").toString();
            }
            java.io.File source = new java.io.File(sourcePath);
            if (source.exists()) {
                output.add(source);
            }
            java.io.File generatedSource = java.nio.file.Paths.get(directory.getAbsolutePath(), "target", "generated-sources").toFile();
            if (generatedSource.exists()) {
                output.add(generatedSource);
            }
            for (spoon.MavenLauncher.InheritanceModel module : modules) {
                output.addAll(module.getSourceDirectories());
            }
            return output;
        }

        public java.util.List<java.io.File> getTestDirectories() {
            java.util.List<java.io.File> output = new java.util.ArrayList<>();
            java.lang.String sourcePath = null;
            org.apache.maven.model.Build build = model.getBuild();
            if (build != null) {
                sourcePath = build.getTestSourceDirectory();
            }
            if (sourcePath == null) {
                sourcePath = java.nio.file.Paths.get(directory.getAbsolutePath(), "src", "test", "java").toString();
            }
            java.io.File source = new java.io.File(sourcePath);
            if (source.exists()) {
                output.add(source);
            }
            java.io.File generatedSource = java.nio.file.Paths.get(directory.getAbsolutePath(), "target", "generated-test-sources").toFile();
            if (generatedSource.exists()) {
                output.add(generatedSource);
            }
            for (spoon.MavenLauncher.InheritanceModel module : modules) {
                output.addAll(module.getTestDirectories());
            }
            return output;
        }

        private java.lang.String extractVariable(java.lang.String value) {
            if (value.startsWith("$")) {
                value = getProperty(value.substring(2, ((value.length()) - 1)));
            }
            return value;
        }

        public java.util.List<java.io.File> getDependencies(boolean isLib) {
            java.util.Set<java.io.File> output = new java.util.HashSet<>();
            org.apache.maven.model.Parent parent = model.getParent();
            if (parent != null) {
                java.lang.String groupId = parent.getGroupId().replace(".", "/");
                java.lang.String version = extractVariable(parent.getVersion());
                if (version.startsWith("[")) {
                    version = version.substring(1, version.indexOf(','));
                }
                java.lang.String fileName = (((parent.getArtifactId()) + "-") + version) + ".jar";
                java.nio.file.Path depPath = java.nio.file.Paths.get(m2RepositoryPath, groupId, parent.getArtifactId(), version, fileName);
                java.io.File jar = depPath.toFile();
                if (jar.exists()) {
                    output.add(jar);
                }
            }
            java.util.List<org.apache.maven.model.Dependency> dependencies = model.getDependencies();
            for (org.apache.maven.model.Dependency dependency : dependencies) {
                java.lang.String groupId = dependency.getGroupId().replace(".", "/");
                if ((dependency.getVersion()) == null) {
                    continue;
                }
                java.lang.String version = extractVariable(dependency.getVersion());
                if (version == null) {
                    spoon.Launcher.LOGGER.warn(("A dependency version cannot be resolved: " + (dependency.toString())));
                    continue;
                }
                if (version.startsWith("[")) {
                    version = version.substring(1, version.indexOf(','));
                }
                if (isLib && (dependency.isOptional())) {
                    continue;
                }
                if (("test".equals(dependency.getScope())) && ((spoon.MavenLauncher.SOURCE_TYPE.APP_SOURCE) == (sourceType))) {
                    continue;
                }
                if (isLib && (("test".equals(dependency.getScope())) || ("provided".equals(dependency.getScope())))) {
                    spoon.Launcher.LOGGER.log(org.apache.log4j.Level.WARN, ("Dependency ignored (scope: provided or test): " + (dependency.toString())));
                    continue;
                }
                java.lang.String fileName = ((dependency.getArtifactId()) + "-") + version;
                java.nio.file.Path depPath = java.nio.file.Paths.get(m2RepositoryPath, groupId, dependency.getArtifactId(), version);
                java.io.File depFile = depPath.toFile();
                if (depFile.exists()) {
                    java.io.File jarFile = java.nio.file.Paths.get(depPath.toString(), (fileName + ".jar")).toFile();
                    if (jarFile.exists()) {
                        output.add(jarFile);
                    }else {
                        getEnvironment().setNoClasspath(true);
                    }
                    try {
                        spoon.MavenLauncher.InheritanceModel dependencyModel = readPOM(java.nio.file.Paths.get(depPath.toString(), (fileName + ".pom")).toString(), null);
                        output.addAll(dependencyModel.getDependencies(true));
                    } catch (java.lang.Exception ignore) {
                    }
                }else {
                    getEnvironment().setNoClasspath(true);
                }
            }
            for (spoon.MavenLauncher.InheritanceModel module : modules) {
                output.addAll(module.getDependencies(isLib));
            }
            return new java.util.ArrayList<>(output);
        }

        private java.lang.String getProperty(java.lang.String key) {
            if ("project.version".equals(key)) {
                if ((model.getVersion()) != null) {
                    return model.getVersion();
                }
            }
            java.lang.String value = model.getProperties().getProperty(key);
            if (value == null) {
                if ((parent) == null) {
                    return null;
                }
                return parent.getProperty(key);
            }
            return value;
        }

        public int getSourceVersion() {
            if ((model.getBuild()) != null) {
                for (org.apache.maven.model.Plugin plugin : model.getBuild().getPlugins()) {
                    if (!("maven-compiler-plugin".equals(plugin.getArtifactId()))) {
                        continue;
                    }
                    org.codehaus.plexus.util.xml.Xpp3Dom configuration = ((org.codehaus.plexus.util.xml.Xpp3Dom) (plugin.getConfiguration()));
                    org.codehaus.plexus.util.xml.Xpp3Dom source = configuration.getChild("source");
                    if (source != null) {
                        return java.lang.Integer.parseInt(extractVariable(source.getValue()).substring(2));
                    }
                    break;
                }
            }
            java.lang.String javaVersion = getProperty("java.version");
            if (javaVersion != null) {
                return java.lang.Integer.parseInt(extractVariable(javaVersion).substring(2));
            }
            javaVersion = getProperty("java.src.version");
            if (javaVersion != null) {
                return java.lang.Integer.parseInt(extractVariable(javaVersion).substring(2));
            }
            javaVersion = getProperty("maven.compiler.source");
            if (javaVersion != null) {
                return java.lang.Integer.parseInt(extractVariable(javaVersion).substring(2));
            }
            javaVersion = getProperty("maven.compile.source");
            if (javaVersion != null) {
                return java.lang.Integer.parseInt(extractVariable(javaVersion).substring(2));
            }
            return getEnvironment().getComplianceLevel();
        }

        @java.lang.Override
        public java.lang.String toString() {
            java.lang.StringBuilder sb = new java.lang.StringBuilder();
            sb.append(model.getName());
            if (modules.isEmpty()) {
                return sb.toString();
            }
            sb.append(" {\n");
            for (int i = 0; i < (modules.size()); i++) {
                spoon.MavenLauncher.InheritanceModel inheritanceModel = modules.get(i);
                java.lang.String child = inheritanceModel.toString();
                for (java.lang.String s : child.split("\n")) {
                    sb.append("\t");
                    sb.append(s);
                    sb.append("\n");
                }
            }
            sb.append("}");
            return sb.toString();
        }
    }
}

