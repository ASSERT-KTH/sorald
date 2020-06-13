package sorald.explanation;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.stringparsers.BooleanStringParser;
import com.martiansoftware.jsap.stringparsers.FileStringParser;
import org.apache.commons.io.FileUtils;
import org.apache.maven.cli.MavenCli;
import sorald.Constants;

import java.io.File;
import java.io.IOException;

public class ExplanationGenerator {
    private static final String CLOVER_REPORT_FILENAME = "clover.xml";
    private static final String CLOVER_REPORT_PATH = "target" + CLOVER_REPORT_FILENAME;

    private static ExplanationGenerator _instance;

    public static ExplanationGenerator getInstance() {
        if (_instance == null)
            _instance = new ExplanationGenerator();
        return _instance;
    }


    private void runTestsAndSaveCloverReport
            (
                    String projectPath,
                    String tmpPath,
                    String executionId
            ) throws Exception {
        MavenCli cli = new MavenCli();
        System.setProperty("maven.multiModuleProjectDirectory", projectPath);
        cli.doMain(new String[]{"clean", "compile"}, projectPath, System.out, System.err);

        String generatedCloverReportPath = projectPath + File.separator + CLOVER_REPORT_PATH;
        File cloverReportFile = new File(generatedCloverReportPath);

        if(!cloverReportFile.exists()){
            throw new Exception("Clover report not generated.");
        }


        File cloverReportDestDir = new File(tmpPath + File.separator + executionId);

        if(!cloverReportDestDir.exists())
            cloverReportDestDir.mkdirs();

        File cloverReportDestFile = new File(cloverReportDestDir.getPath()
                + File.separator + (CLOVER_REPORT_FILENAME));
        FileUtils.copyFile(cloverReportFile, cloverReportDestFile);
    }



    public static JSAP defineArgs() throws JSAPException {
        JSAP jsap = new JSAP();

        FlaggedOption opt = new FlaggedOption(Constants.ARG_ORIGINAL_FILES_PATH);
        opt.setLongFlag(Constants.ARG_ORIGINAL_FILES_PATH);
        opt.setStringParser(FileStringParser.getParser().setMustExist(true));
        opt.setRequired(false);
        opt.setHelp("The path to the file or folder to be analyzed.");
        jsap.registerParameter(opt);

        Switch sw = new Switch("help");
        sw.setShortFlag('h');
        sw.setLongFlag("help");
        sw.setDefault("false");
        jsap.registerParameter(sw);

        return jsap;
    }

    public static void main(String[] args) throws Exception {
        ExplanationGenerator.getInstance().runTestsAndSaveCloverReport("C:\\other\\daneshgah\\" +
                "phd-kth\\projects\\explanation generation\\tmp\\Tardis",
                "C:\\other\\daneshgah\\phd-kth\\projects\\explanation generation\\tmp\\temp",
                "tardis-org");
    }
}
