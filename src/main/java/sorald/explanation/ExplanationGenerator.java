package sorald.explanation;

import org.apache.commons.io.FileUtils;
import org.apache.maven.cli.MavenCli;
import sorald.explanation.models.CloverReportForFile;
import sorald.explanation.models.ExecutionTraceChanges;
import sorald.explanation.utils.CloverHelper;
import sorald.explanation.utils.GumtreeComparison;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.List;

public class ExplanationGenerator {
    private static final String EXPLANATION_TEMPLATE_File = "explanation-template.html";
    private static final String CLOVER_REPORT_FILENAME = "clover.xml";
    private static final String CLOVER_REPORT_PATH = "target/site/clover/" + CLOVER_REPORT_FILENAME;
    private static final String COVERAGE_CHANGED_LINE_TEMPLATE = "<span style='background-color:red'>{{code}}</span>";
    private static final String COVERED_BY_BOTH_LINE_TEMPLATE = "<span style='background-color:#90EE90'>{{code}}</span>";
    private static final String LINE_ONLY_IN_ONE_VERSION_TEMPLATE = "<span style='background-color:yellow'>{{code}}</span>";
    private static final String LINE_CODE_PLACEHOLDER = "{{code}}";
    private static final String OLD_CODE_PLACEHOLDER = "{{old-code}}";
    private static final String NEW_CODE_PLACEHOLDER = "{{new-code}}";

    private static ExplanationGenerator _instance;

    public static ExplanationGenerator getInstance() {
        if (_instance == null)
            _instance = new ExplanationGenerator();
        return _instance;
    }

    private void runTestsAndSaveCloverReport
            (
                    String projectPath,
                    String outputPath
            ) throws Exception {
        MavenCli cli = new MavenCli();
        System.setProperty("maven.multiModuleProjectDirectory", projectPath);
        cli.doMain(new String[]{"clean", "clover:setup", "test", "clover:aggregate", "clover:clover",
                        "-DtestFailureIgnore=true", "-fail-never"},
                projectPath, System.out, System.err);

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

        FileUtils.writeStringToFile(new File(outputPath), toBePrinted);
    }

    public static void main(String[] args) throws Exception {
        String oldRepoPath = "C:\\other\\daneshgah\\phd-kth\\projects\\explanation generation\\tmp\\photon",
                newRepoPath = "C:\\other\\daneshgah\\phd-kth\\projects\\explanation generation\\tmp\\photon2",
                changedFilePath = "src\\main\\java\\de\\komoot\\photon\\PhotonDoc.java",
                tmpDirPath = "C:\\other\\daneshgah\\phd-kth\\projects\\explanation generation\\tmp\\temp",
                outputPath = "C:\\other\\daneshgah\\phd-kth\\projects\\explanation generation\\tmp\\temp\\explanation.html";

        String oldReportFileName = "old-clover-report", newReportFileName = "new-clover-report";


        String oldReportFilePath = tmpDirPath + File.separator + oldReportFileName,
                newReportFilePath = tmpDirPath + File.separator + newReportFileName;
        String oldChangedFilePath = oldRepoPath + File.separator + changedFilePath,
                newChangedFilePath = newRepoPath + File.separator + changedFilePath;


//        ExplanationGenerator.getInstance().printCloverReport(oldRepoPath, oldReportFilePath);
//        ExplanationGenerator.getInstance().printCloverReport(newRepoPath, newReportFilePath);


        CloverReportForFile oldReport = new CloverReportForFile(oldReportFilePath, oldChangedFilePath),
                newReport = new CloverReportForFile(newReportFilePath, newChangedFilePath);


        GumtreeComparison gtc = new GumtreeComparison(oldChangedFilePath, newChangedFilePath);

        ExecutionTraceChanges etc = new ExecutionTraceChanges(oldReport, newReport, gtc);

        ExplanationGenerator.getInstance().generateOutput(oldChangedFilePath, newChangedFilePath, outputPath, etc);
    }
}
