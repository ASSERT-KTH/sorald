package sorald.explanation;

import sorald.Constants;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.List;

public class ExplanationGenerator {
    private static final String DIFFERENT_REPORT_SIZE_MESSAGE =
            "TWO DIFFS HAVE DIFFERENT NUMBER OF LINES";

    private static ExplanationGenerator _instance;

    public static ExplanationGenerator getInstance() {
        if (_instance == null)
            _instance = new ExplanationGenerator();
        return _instance;
    }

    public void generateExecutionTraceDiff
            (
                    String oldJacocoReportPath,
                    String newJacocoReportPath,
                    String outputPath
            ) throws IOException {
        PrintWriter pw = new PrintWriter(new File(outputPath));

        List<String> oldReportLines = Files.readAllLines(new File(oldJacocoReportPath).toPath()),
                newReportLines = Files.readAllLines(new File(newJacocoReportPath).toPath());

        if (oldReportLines.size() != newReportLines.size()) {
            pw.println(DIFFERENT_REPORT_SIZE_MESSAGE);
        } else {

            for (int i = 0; i < oldReportLines.size(); i++) {
                String oldLine = oldReportLines.get(i),
                        newLine = newReportLines.get(i);
                if (oldLine.startsWith(JacocoConstants.FULL_COVERED_CLASS)
                        || oldLine.startsWith(JacocoConstants.PARTIAL_COVERED_CLASS)) {
                    if (newLine.startsWith(JacocoConstants.UNCOVERED_CLASS)) {

                        pw.println(newLine.replace(JacocoConstants.UNCOVERED_CLASS,
                                JacocoConstants.COVERED_TO_UNCOVERED_CLASS));
                    }
                } else if (oldLine.startsWith(JacocoConstants.UNCOVERED_CLASS)) {
                    if (newLine.startsWith(JacocoConstants.FULL_COVERED_CLASS)
                            || newLine.startsWith(JacocoConstants.PARTIAL_COVERED_CLASS)) {

                        pw.println(newLine.replace(JacocoConstants.FULL_COVERED_CLASS,
                                JacocoConstants.UNCOVERED_TO_COVERED_CLASS)
                                .replace(JacocoConstants.PARTIAL_COVERED_CLASS,
                                        JacocoConstants.UNCOVERED_TO_COVERED_CLASS));
                    }
                }
            }

        }

        pw.flush();
        pw.close();
    }

    public static void main(String[] args) throws Exception {
//        ExplanationGenerator.getInstance().generateExecutionTraceDiff(
//                Constants.PATH_TO_RESOURCES_FOLDER + "explanation/coverage_reports/Bukkit/Color.java_s.html",
//                Constants.PATH_TO_RESOURCES_FOLDER + "explanation/coverage_reports/Bukkit/Color.java_t.html",
//                Constants.PATH_TO_RESOURCES_FOLDER + "explanation/coverage_reports/Bukkit/Color.java_r.html");
    }
}
