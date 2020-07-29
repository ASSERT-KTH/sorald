package sorald.explanation;

import com.google.gson.*;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.xml.sax.SAXException;
import sorald.explanation.utils.CloverHelper;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

public class RepoCompilationAnalyzerMain {
    public static void main(String[] args) throws Exception {
//        args = new String[] {"C:\\other\\daneshgah\\phd-kth\\projects\\explanation generation\\tmp\\repos.txt",
//                "C:\\other\\daneshgah\\phd-kth\\projects\\explanation generation\\tmp\\repo",
//                "C:\\other\\daneshgah\\phd-kth\\projects\\explanation generation\\tmp\\output"};
//        String reposPath = args[0];
//        File cloneDir = new File(args[1]), outputDir = new File(args[2]);
//        String resultsPath = args[2] + File.separator + "results.json";
//        String mavenPath = args[3];
//
//        analyzeRepos(reposPath, cloneDir, outputDir, resultsPath, mavenPath);

//        summarizeResults();
    }

    private static void summarizeResults() throws IOException, ParserConfigurationException, SAXException {
        File resultsDir = new File("experimentation/compilable-repos");

        File[] files = resultsDir.listFiles();

        Map<String, Double> coveragePerCommitId = new HashMap<>();
        for (File file : files) {
            if (!file.getName().startsWith("partial-result") && !file.getName().startsWith("results")) {
                double coverage = CloverHelper.getInstance().getTotalCoverage(file);
                coveragePerCommitId.put(file.getName().split("-")[0], coverage);
            }
        }

        Map<String, Boolean> results = new HashMap<>();
        for (File file : files) {
            if (file.getName().startsWith("partial-result")) {
                Scanner sc = new Scanner(file);

                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    String projectAddress = line.split(" ")[0];
                    String commitId = line.split(" ")[1];
                    String isCloverGenerated = line.split(" ")[2];

                    double coverage = coveragePerCommitId.containsKey(commitId) ? coveragePerCommitId.get(commitId) : -1;

                    results.put(projectAddress + ":::" + commitId + ":::" + coverage,
                            Boolean.parseBoolean(isCloverGenerated));
                }

                sc.close();
            } else if (file.getName().startsWith("results")) {
                continue;
            }
        }

        printResults("experimentation/compilable-repos/results.json", results);
    }

    private static void analyzeRepos
            (
                    String reposPath,
                    File cloneDir,
                    File outputDir,
                    String resultsPath,
                    String mavenPath
            ) throws IOException {
        List<String> repos = new ArrayList<>();
        Scanner sc = new Scanner(new File(reposPath));

        while (sc.hasNextLine()) {
            repos.add(sc.nextLine());
        }

        sc.close();

        Map<String, Boolean> result = new HashMap<>();

        for (String repo : repos) {
            FileUtils.cleanDirectory(cloneDir);

            String headCommitId = null;
//            String headCommitId = "f42cbb3aec611ea9e7878290af41d3bdf2be2f7c";

            try {
                Git git = Git.cloneRepository()
                        .setURI(repo)
                        .setDirectory(cloneDir)
                        .call();

                headCommitId = git.getRepository().getAllRefs().get("HEAD").getObjectId().getName();

                git.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("Project cloned with size of: " + FileUtils.sizeOfDirectory(cloneDir));

            String cloverOutputPath = outputDir.getPath() + File.separator + headCommitId + "-clover.xml";

            String exceptionMessage = null;
            try {
                ExplanationGenerator.getInstance(mavenPath).runTestsAndSaveCloverReport(cloneDir.getPath(), cloverOutputPath);
            } catch (Exception ex) {
                ex.printStackTrace();
                exceptionMessage = ex.getMessage();
            }

            Boolean isCloverReportGenerated = new File(cloverOutputPath).exists();
            result.put(repo + ":::" + headCommitId, isCloverReportGenerated);

            PrintWriter pw = new PrintWriter(new FileWriter(outputDir.getPath() + File.separator
                    + "partial-result", true));
            pw.println(repo + " " + headCommitId + " " + isCloverReportGenerated + (exceptionMessage == null ? "" :
                    " " + exceptionMessage));
            pw.flush();
            pw.close();
        }

        printResults(resultsPath, result);
    }

    private static void printResults(String resultsPath, Map<String, Boolean> result) {
        JsonObject root = new JsonObject();
        JsonArray results = new JsonArray();
        for (Map.Entry<String, Boolean> entry : result.entrySet()) {
            JsonObject repoResult = new JsonObject();
            repoResult.addProperty("project-addr", entry.getKey().split(":::")[0]);
            repoResult.addProperty("commit-id", entry.getKey().split(":::")[1]);
            if (entry.getKey().split(":::").length > 2) {
                repoResult.addProperty("statement-coverage",
                        Double.parseDouble(entry.getKey().split(":::")[2]));
            }
            repoResult.addProperty("is-clover-generated", entry.getValue());
            results.add(repoResult);
        }
        root.add("results", results);
        saveToJsonFile(root, resultsPath);
    }

    private static void saveToJsonFile(JsonObject root, String outputPath) {
        try {
            FileWriter fw = new FileWriter(outputPath);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonParser jp = new JsonParser();
            JsonElement je = jp.parse(root.toString());
            String prettyJsonString = gson.toJson(je);
            fw.write(prettyJsonString);

            fw.flush();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
