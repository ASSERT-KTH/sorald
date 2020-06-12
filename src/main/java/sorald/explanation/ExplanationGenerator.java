package sorald.explanation;

import java.io.File;

public class ExplanationGenerator {
    private static final String DIFFERENT_REPORT_SIZE_MESSAGE =
            "TWO DIFFS HAVE DIFFERENT NUMBER OF LINES";

    private static ExplanationGenerator _instance;

    public static ExplanationGenerator getInstance() {
        if (_instance == null)
            _instance = new ExplanationGenerator();
        return _instance;
    }

    public static void main(String[] args) throws Exception {
        CloverHelper.getInstance().addCloverPluginToPom(
                new File("C:\\Users\\kheso\\Desktop\\pom.xml"),
                new File("C:\\Users\\kheso\\Desktop\\pom.xml"));
    }
}
