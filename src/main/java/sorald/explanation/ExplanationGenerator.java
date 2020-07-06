package sorald.explanation;

import org.apache.commons.io.FileUtils;
import sorald.explanation.models.CloverReportForFile;
import sorald.explanation.models.ExecutionTraceChanges;
import sorald.explanation.utils.CloverHelper;
import sorald.explanation.utils.GumtreeComparison;

import java.io.*;
import java.net.URISyntaxException;
import java.util.List;

public class ExplanationGenerator {
    private static final String MAVEN_CLOVER_COMMAND = "{{maven-path}} " +
            "-f \"{{path}}\" clean clover:setup test clover:aggregate " +
            "clover:clover -DtestFailureIgnore=true -fail-never";
    private static final String PATH_PLACEHOLDER = "{{path}}";
    private static final String MAVEN_PATH_PLACEHOLDER = "{{maven-path}}";
    private static final String EXPLANATION_TEMPLATE_File = "explanation-template.html";
    private static final String CLOVER_REPORT_FILENAME = "clover.xml";
    private static final String CLOVER_REPORT_PATH = "target/site/clover/" + CLOVER_REPORT_FILENAME;
    private static final String COVERAGE_CHANGED_LINE_TEMPLATE = "<span style='background-color:red'>{{code}}</span>";
    private static final String COVERED_BY_BOTH_LINE_TEMPLATE = "<span style='background-color:#90EE90'>{{code}}</span>";
    private static final String LINE_ONLY_IN_ONE_VERSION_TEMPLATE = "<span style='background-color:yellow'>{{code}}</span>";
    private static final String LINE_CODE_PLACEHOLDER = "{{code}}";
    private static final String OLD_CODE_PLACEHOLDER = "{{old-code}}";
    private static final String NEW_CODE_PLACEHOLDER = "{{new-code}}";
    private static final String EXPLANATION_FILENAME = "explanation.html";
    private static final String OLD_CLOVER_REPORT_FILENAME = "old-clover-report.xml";
    private static final String NEW_CLOVER_REPORT_FILENAME = "new-clover-report.xml";

    private String mavenPath;

    private static ExplanationGenerator _instance;

    public static ExplanationGenerator getInstance(String mavenPath) {
        if (_instance == null)
            _instance = new ExplanationGenerator(mavenPath);
        return _instance;
    }

    public ExplanationGenerator(String mavenPath){
        this.mavenPath = mavenPath;
    }

    public void runTestsAndSaveCloverReport
            (
                    String projectPath,
                    String outputPath
            ) throws Exception {

        Runtime rt = Runtime.getRuntime();

        String mavenCommand = MAVEN_CLOVER_COMMAND.replace(MAVEN_PATH_PLACEHOLDER, mavenPath)
                .replace(PATH_PLACEHOLDER, projectPath);
        System.out.println("Executing: " + mavenCommand);
        Process pr = rt.exec(mavenCommand);

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String s = null;
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }

        String generatedCloverReportPath = projectPath + File.separator + CLOVER_REPORT_PATH;
        File cloverReportFile = new File(generatedCloverReportPath);

        if (!cloverReportFile.exists()) {
            throw new Exception("Clover report not generated.");
        }


        File cloverReportDestFile = new File(outputPath);
        FileUtils.copyFile(cloverReportFile, cloverReportDestFile);
    }

    private void printCloverReport(String projectPath, String outputPath) throws Exception {
        CloverHelper.getInstance().addCloverPluginToPom(new File(projectPath + File.separator + "pom.xml"),
                new File(projectPath + File.separator + "pom.xml"));

        runTestsAndSaveCloverReport(projectPath, outputPath);
    }

    private void generateOutput
            (
                    String oldSourcePath,
                    String newSourcePath,
                    String outputPath,
                    ExecutionTraceChanges traceChanges
            ) throws IOException, URISyntaxException {
        String toBePrinted = FileUtils.readFileToString(new File(getClass().getClassLoader()
                .getResource(EXPLANATION_TEMPLATE_File).toURI()), "UTF-8");

        List<String> oldLines = FileUtils.readLines(new File(oldSourcePath), "UTF-8"),
                newLines = FileUtils.readLines(new File(newSourcePath), "UTF-8");

        StringWriter oldCodeSw = new StringWriter();
        PrintWriter pw = new PrintWriter(oldCodeSw);

        for (int i = 0; i < oldLines.size(); i++) {
            String line = oldLines.get(i);
            int lineNum = i + 1;

            if (traceChanges.getNewlyUncoveredLines().contains(lineNum)) {
                pw.println(COVERAGE_CHANGED_LINE_TEMPLATE.replace(LINE_CODE_PLACEHOLDER, line));
            } else if(traceChanges.getLinesOnlyInSrc().contains(lineNum)) {
                pw.println(LINE_ONLY_IN_ONE_VERSION_TEMPLATE.replace(LINE_CODE_PLACEHOLDER, line));
            } else if (traceChanges.getSrcLinesCoveredInBoth().contains(lineNum)) {
                pw.println(COVERED_BY_BOTH_LINE_TEMPLATE.replace(LINE_CODE_PLACEHOLDER, line));
            } else {
                pw.println(line);
            }
        }

        pw.flush();
        pw.close();

        toBePrinted = toBePrinted.replace(OLD_CODE_PLACEHOLDER, oldCodeSw.toString());
        oldCodeSw.close();

        StringWriter newCodeSw = new StringWriter();
        pw = new PrintWriter(newCodeSw);

        for (int i = 0; i < newLines.size(); i++) {
            String line = newLines.get(i);
            int lineNum = i + 1;

            if (traceChanges.getNewlyCoveredLines().contains(lineNum)) {
                pw.println(COVERAGE_CHANGED_LINE_TEMPLATE.replace(LINE_CODE_PLACEHOLDER, line));
            } else if(traceChanges.getLinesOnlyInDst().contains(lineNum)) {
                pw.println(LINE_ONLY_IN_ONE_VERSION_TEMPLATE.replace(LINE_CODE_PLACEHOLDER, line));
            } else if (traceChanges.getDstLinesCoveredInBoth().contains(lineNum)) {
                pw.println(COVERED_BY_BOTH_LINE_TEMPLATE.replace(LINE_CODE_PLACEHOLDER, line));
            } else {
                pw.println(line);
            }
        }

        pw.flush();
        pw.close();

        toBePrinted = toBePrinted.replace(NEW_CODE_PLACEHOLDER, newCodeSw.toString());
        newCodeSw.close();

        FileUtils.writeStringToFile(new File(outputPath + File.separator + EXPLANATION_FILENAME), toBePrinted);
    }

    private void generateExplanation
            (
                    String oldRepoPath,
                    String newRepoPath,
                    String changedFilePath,
                    String outputDirPath
            ) throws Exception {
        String oldReportFilePath = outputDirPath + File.separator + OLD_CLOVER_REPORT_FILENAME,
                newReportFilePath = outputDirPath + File.separator + NEW_CLOVER_REPORT_FILENAME;
        String oldChangedFilePath = oldRepoPath + File.separator + changedFilePath,
                newChangedFilePath = newRepoPath + File.separator + changedFilePath;


        // computing and printing clover-report for old and new versions of the project
        printCloverReport(oldRepoPath, oldReportFilePath);
        printCloverReport(newRepoPath, newReportFilePath);


        // loading the clover report to be used in next steps
        CloverReportForFile oldReport = new CloverReportForFile(oldReportFilePath, oldChangedFilePath),
                newReport = new CloverReportForFile(newReportFilePath, newChangedFilePath);


        // using gumtree-ast-diff to map the old and new code elements
        GumtreeComparison gtc = new GumtreeComparison(oldChangedFilePath, newChangedFilePath);


        // computing the execution trace changes
        ExecutionTraceChanges etc = new ExecutionTraceChanges(oldReport, newReport, gtc);


        // printing the execution trace changes in the desired format
        generateOutput(oldChangedFilePath, newChangedFilePath, outputDirPath, etc);
    }

    public static void main(String[] args) throws Exception {

    }

    private static void testGenerateExplanation() throws Exception {
        String oldRepoPath = "C:\\other\\daneshgah\\phd-kth\\projects\\explanation generation\\tmp\\photon",
                newRepoPath = "C:\\other\\daneshgah\\phd-kth\\projects\\explanation generation\\tmp\\photon2",
                changedFilePath = "src\\main\\java\\de\\komoot\\photon\\PhotonDoc.java",
                outputDirPath = "C:\\other\\daneshgah\\phd-kth\\projects\\explanation generation\\tmp\\temp";


        getInstance("C:\\Program Files (x86)\\apache\\apache-maven-3.6.2\\bin\\mvn.cmd")
                .generateExplanation(oldRepoPath, newRepoPath, changedFilePath, outputDirPath);
        return;
    }

    public String getMavenPath() {
        return mavenPath;
    }

    public void setMavenPath(String mavenPath) {
        this.mavenPath = mavenPath;
    }
}
