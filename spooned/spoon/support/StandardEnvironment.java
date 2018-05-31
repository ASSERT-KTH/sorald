package spoon.support;


public class StandardEnvironment implements java.io.Serializable , spoon.compiler.Environment {
    private static final long serialVersionUID = 1L;

    public static final int DEFAULT_CODE_COMPLIANCE_LEVEL = 8;

    private transient spoon.processing.FileGenerator<? extends spoon.reflect.declaration.CtElement> defaultFileGenerator;

    private int errorCount = 0;

    transient spoon.processing.ProcessingManager manager;

    private boolean processingStopped = false;

    private boolean autoImports = false;

    private int warningCount = 0;

    private java.lang.String[] sourceClasspath = null;

    private boolean preserveLineNumbers = false;

    private boolean copyResources = true;

    private boolean enableComments = false;

    private transient org.apache.log4j.Logger logger = spoon.Launcher.LOGGER;

    private org.apache.log4j.Level level = org.apache.log4j.Level.OFF;

    private boolean shouldCompile = false;

    private boolean skipSelfChecks = false;

    private transient spoon.experimental.modelobs.FineModelChangeListener modelChangeListener = new spoon.experimental.modelobs.EmptyModelChangeListener();

    private transient java.nio.charset.Charset encoding = java.nio.charset.Charset.defaultCharset();

    private int complianceLevel = spoon.support.StandardEnvironment.DEFAULT_CODE_COMPLIANCE_LEVEL;

    private transient spoon.support.OutputDestinationHandler outputDestinationHandler = new spoon.support.DefaultOutputDestinationHandler(new java.io.File(spoon.Launcher.OUTPUTDIR), this);

    private spoon.OutputType outputType = spoon.OutputType.CLASSES;

    private java.lang.Boolean noclasspath = null;

    public StandardEnvironment() {
    }

    @java.lang.Override
    public void debugMessage(java.lang.String message) {
        logger.debug(message);
    }

    @java.lang.Override
    public boolean isAutoImports() {
        return autoImports;
    }

    @java.lang.Override
    public void setAutoImports(boolean autoImports) {
        this.autoImports = autoImports;
    }

    @java.lang.Override
    public spoon.processing.FileGenerator<? extends spoon.reflect.declaration.CtElement> getDefaultFileGenerator() {
        return defaultFileGenerator;
    }

    @java.lang.Override
    public org.apache.log4j.Level getLevel() {
        return this.level;
    }

    @java.lang.Override
    public void setLevel(java.lang.String level) {
        this.level = toLevel(level);
        logger.setLevel(this.level);
    }

    @java.lang.Override
    public boolean shouldCompile() {
        return shouldCompile;
    }

    @java.lang.Override
    public void setShouldCompile(boolean shouldCompile) {
        this.shouldCompile = shouldCompile;
    }

    @java.lang.Override
    public boolean checksAreSkipped() {
        return skipSelfChecks;
    }

    @java.lang.Override
    public void setSelfChecks(boolean skip) {
        skipSelfChecks = skip;
    }

    @java.lang.Override
    public void disableConsistencyChecks() {
        skipSelfChecks = true;
    }

    private org.apache.log4j.Level toLevel(java.lang.String level) {
        if ((level == null) || (level.isEmpty())) {
            throw new spoon.SpoonException("Wrong level given at Spoon.");
        }
        return org.apache.log4j.Level.toLevel(level, org.apache.log4j.Level.ALL);
    }

    @java.lang.Override
    public spoon.processing.ProcessingManager getManager() {
        return manager;
    }

    transient java.util.Map<java.lang.String, spoon.processing.ProcessorProperties> processorProperties = new java.util.TreeMap<>();

    @java.lang.Override
    public spoon.processing.ProcessorProperties getProcessorProperties(java.lang.String processorName) throws java.lang.Exception {
        if (processorProperties.containsKey(processorName)) {
            return processorProperties.get(processorName);
        }
        return null;
    }

    @java.lang.Override
    public boolean isProcessingStopped() {
        return processingStopped;
    }

    private void prefix(java.lang.StringBuffer buffer, org.apache.log4j.Level level) {
        if (level == (org.apache.log4j.Level.ERROR)) {
            buffer.append("error: ");
            (errorCount)++;
        }else
            if (level == (org.apache.log4j.Level.WARN)) {
                buffer.append("warning: ");
                (warningCount)++;
            }

    }

    @java.lang.Override
    public void report(spoon.processing.Processor<?> processor, org.apache.log4j.Level level, spoon.reflect.declaration.CtElement element, java.lang.String message) {
        java.lang.StringBuffer buffer = new java.lang.StringBuffer();
        prefix(buffer, level);
        buffer.append(message);
        try {
            spoon.reflect.declaration.CtType<?> type = (element instanceof spoon.reflect.declaration.CtType) ? ((spoon.reflect.declaration.CtType<?>) (element)) : element.getParent(spoon.reflect.declaration.CtType.class);
            spoon.reflect.cu.SourcePosition sp = element.getPosition();
            if (sp == null) {
                buffer.append(" (Unknown Source)");
            }else {
                buffer.append(((" at " + (type.getQualifiedName())) + "."));
                spoon.reflect.declaration.CtExecutable<?> exe = (element instanceof spoon.reflect.declaration.CtExecutable) ? ((spoon.reflect.declaration.CtExecutable<?>) (element)) : element.getParent(spoon.reflect.declaration.CtExecutable.class);
                if (exe != null) {
                    buffer.append(exe.getSimpleName());
                }
                buffer.append((((("(" + (sp.getFile().getName())) + ":") + (sp.getLine())) + ")"));
            }
        } catch (spoon.reflect.declaration.ParentNotInitializedException e) {
            buffer.append(" (invalid parent)");
        }
        print(buffer.toString(), level);
    }

    @java.lang.Override
    public void report(spoon.processing.Processor<?> processor, org.apache.log4j.Level level, spoon.reflect.declaration.CtElement element, java.lang.String message, spoon.processing.ProblemFixer<?>... fixes) {
        report(processor, level, element, message);
    }

    @java.lang.Override
    public void report(spoon.processing.Processor<?> processor, org.apache.log4j.Level level, java.lang.String message) {
        java.lang.StringBuffer buffer = new java.lang.StringBuffer();
        prefix(buffer, level);
        buffer.append(message);
        print(buffer.toString(), level);
    }

    private void print(java.lang.String message, org.apache.log4j.Level level) {
        if (level.equals(org.apache.log4j.Level.ERROR)) {
            logger.error(message);
        }else
            if (level.equals(org.apache.log4j.Level.WARN)) {
                logger.warn(message);
            }else
                if (level.equals(org.apache.log4j.Level.DEBUG)) {
                    logger.debug(message);
                }else
                    if (level.equals(org.apache.log4j.Level.INFO)) {
                        logger.info(message);
                    }



    }

    public void reportEnd() {
        logger.info("end of processing: ");
        if ((warningCount) > 0) {
            logger.info(((warningCount) + " warning"));
            if ((warningCount) > 1) {
                logger.info("s");
            }
            if ((errorCount) > 0) {
                logger.info(", ");
            }
        }
        if ((errorCount) > 0) {
            logger.info(((errorCount) + " error"));
            if ((errorCount) > 1) {
                logger.info("s");
            }
        }
        if (((errorCount) + (warningCount)) > 0) {
            logger.info("\n");
        }else {
            logger.info("no errors, no warnings");
        }
    }

    public void reportProgressMessage(java.lang.String message) {
        logger.info(message);
    }

    public void setDebug(boolean debug) {
    }

    public void setDefaultFileGenerator(spoon.processing.FileGenerator<? extends spoon.reflect.declaration.CtElement> defaultFileGenerator) {
        this.defaultFileGenerator = defaultFileGenerator;
    }

    public void setManager(spoon.processing.ProcessingManager manager) {
        this.manager = manager;
    }

    public void setProcessingStopped(boolean processingStopped) {
        this.processingStopped = processingStopped;
    }

    public void setVerbose(boolean verbose) {
    }

    public int getComplianceLevel() {
        return complianceLevel;
    }

    public void setComplianceLevel(int level) {
        complianceLevel = level;
    }

    public void setProcessorProperties(java.lang.String processorName, spoon.processing.ProcessorProperties prop) {
        processorProperties.put(processorName, prop);
    }

    boolean useTabulations = false;

    public boolean isUsingTabulations() {
        return useTabulations;
    }

    public void useTabulations(boolean tabulation) {
        useTabulations = tabulation;
    }

    int tabulationSize = 4;

    public int getTabulationSize() {
        return tabulationSize;
    }

    public void setTabulationSize(int tabulationSize) {
        this.tabulationSize = tabulationSize;
    }

    private transient java.lang.ClassLoader classloader;

    private transient java.lang.ClassLoader inputClassloader;

    @java.lang.Override
    public void setInputClassLoader(java.lang.ClassLoader aClassLoader) {
        if (aClassLoader instanceof java.net.URLClassLoader) {
            final java.net.URL[] urls = ((java.net.URLClassLoader) (aClassLoader)).getURLs();
            if ((urls != null) && ((urls.length) > 0)) {
                boolean onlyFileURLs = true;
                for (java.net.URL url : urls) {
                    if (!(url.getProtocol().equals("file"))) {
                        onlyFileURLs = false;
                    }
                }
                if (onlyFileURLs) {
                    java.util.List<java.lang.String> classpath = new java.util.ArrayList<>();
                    for (java.net.URL url : urls) {
                        classpath.add(url.getPath());
                    }
                    setSourceClasspath(classpath.toArray(new java.lang.String[0]));
                }else {
                    throw new spoon.SpoonException("Spoon does not support a URLClassLoader containing other resources than local file.");
                }
            }
            return;
        }
        this.classloader = aClassLoader;
    }

    @java.lang.Override
    public java.lang.ClassLoader getInputClassLoader() {
        if ((classloader) != null) {
            return classloader;
        }
        if ((inputClassloader) == null) {
            inputClassloader = new java.net.URLClassLoader(urlClasspath(), java.lang.Thread.currentThread().getContextClassLoader());
        }
        return inputClassloader;
    }

    public java.net.URL[] urlClasspath() {
        java.lang.String[] classpath = getSourceClasspath();
        int length = (classpath == null) ? 0 : classpath.length;
        java.net.URL[] urls = new java.net.URL[length];
        for (int i = 0; i < length; i += 1) {
            try {
                urls[i] = new java.io.File(classpath[i]).toURI().toURL();
            } catch (java.net.MalformedURLException e) {
                throw new java.lang.IllegalStateException(("Invalid classpath: " + (java.util.Arrays.toString(classpath))), e);
            }
        }
        return urls;
    }

    @java.lang.Override
    public java.lang.String[] getSourceClasspath() {
        return sourceClasspath;
    }

    @java.lang.Override
    public void setSourceClasspath(java.lang.String[] sourceClasspath) {
        verifySourceClasspath(sourceClasspath);
        this.sourceClasspath = sourceClasspath;
        this.inputClassloader = null;
    }

    private void verifySourceClasspath(java.lang.String[] sourceClasspath) throws spoon.compiler.InvalidClassPathException {
        for (java.lang.String classPathElem : sourceClasspath) {
            java.io.File classOrJarFolder = new java.io.File(classPathElem);
            if (!(classOrJarFolder.exists())) {
                throw new spoon.compiler.InvalidClassPathException((classPathElem + " does not exist, it is not a valid folder"));
            }
            if (classOrJarFolder.isDirectory()) {
                spoon.compiler.SpoonFolder tmp = new spoon.support.compiler.FileSystemFolder(classOrJarFolder);
                java.util.List<spoon.compiler.SpoonFile> javaFiles = tmp.getAllJavaFiles();
                if ((javaFiles.size()) > 0) {
                    logger.warn((("You're trying to give source code in the classpath, this should be given to " + "addInputSource ") + javaFiles));
                }
                logger.warn((("You specified the directory " + (classOrJarFolder.getPath())) + " in source classpath, please note that only class files will be considered. Jars and subdirectories will be ignored."));
            }else
                if (classOrJarFolder.getName().endsWith(".class")) {
                    throw new spoon.compiler.InvalidClassPathException(".class files are not accepted in source classpath.");
                }

        }
    }

    @java.lang.Override
    public int getErrorCount() {
        return errorCount;
    }

    @java.lang.Override
    public int getWarningCount() {
        return warningCount;
    }

    @java.lang.Override
    public boolean isPreserveLineNumbers() {
        return preserveLineNumbers;
    }

    @java.lang.Override
    public void setPreserveLineNumbers(boolean preserveLineNumbers) {
        this.preserveLineNumbers = preserveLineNumbers;
    }

    @java.lang.Override
    public void setNoClasspath(boolean option) {
        noclasspath = option;
    }

    @java.lang.Override
    public boolean getNoClasspath() {
        if ((this.noclasspath) == null) {
            logger.warn("Spoon is currently use with the default noClasspath option set as true. Read the documentation for more information: http://spoon.gforge.inria.fr/launcher.html#about-the-classpath");
            this.noclasspath = true;
        }
        return noclasspath;
    }

    @java.lang.Override
    public boolean isCopyResources() {
        return copyResources;
    }

    @java.lang.Override
    public void setCopyResources(boolean copyResources) {
        this.copyResources = copyResources;
    }

    @java.lang.Override
    public boolean isCommentsEnabled() {
        return enableComments;
    }

    @java.lang.Override
    public void setCommentEnabled(boolean commentEnabled) {
        this.enableComments = commentEnabled;
    }

    private java.lang.String binaryOutputDirectory = spoon.Launcher.SPOONED_CLASSES;

    @java.lang.Override
    public void setBinaryOutputDirectory(java.lang.String s) {
        this.binaryOutputDirectory = s;
    }

    @java.lang.Override
    public java.lang.String getBinaryOutputDirectory() {
        return binaryOutputDirectory;
    }

    @java.lang.Override
    public void setSourceOutputDirectory(java.io.File directory) {
        if (directory == null) {
            throw new spoon.SpoonException("You must specify a directory.");
        }
        if (directory.isFile()) {
            throw new spoon.SpoonException("Output must be a directory");
        }
        try {
            this.outputDestinationHandler = new spoon.support.DefaultOutputDestinationHandler(directory.getCanonicalFile(), this);
        } catch (java.io.IOException e) {
            spoon.Launcher.LOGGER.error(e.getMessage(), e);
            throw new spoon.SpoonException(e);
        }
    }

    @java.lang.Override
    public java.io.File getSourceOutputDirectory() {
        return this.outputDestinationHandler.getDefaultOutputDirectory();
    }

    @java.lang.Override
    public void setOutputDestinationHandler(spoon.support.OutputDestinationHandler outputDestinationHandler) {
        this.outputDestinationHandler = outputDestinationHandler;
    }

    @java.lang.Override
    public spoon.support.OutputDestinationHandler getOutputDestinationHandler() {
        return outputDestinationHandler;
    }

    @java.lang.Override
    public spoon.experimental.modelobs.FineModelChangeListener getModelChangeListener() {
        return modelChangeListener;
    }

    @java.lang.Override
    public void setModelChangeListener(spoon.experimental.modelobs.FineModelChangeListener modelChangeListener) {
        this.modelChangeListener = modelChangeListener;
    }

    @java.lang.Override
    public java.nio.charset.Charset getEncoding() {
        return this.encoding;
    }

    @java.lang.Override
    public void setEncoding(java.nio.charset.Charset encoding) {
        this.encoding = encoding;
    }

    @java.lang.Override
    public void setOutputType(spoon.OutputType outputType) {
        this.outputType = outputType;
    }

    @java.lang.Override
    public spoon.OutputType getOutputType() {
        return this.outputType;
    }
}

