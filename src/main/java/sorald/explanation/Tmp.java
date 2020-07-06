package sorald.explanation;

import com.google.gson.*;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

public class Tmp {
    public static void main(String[] args) throws Exception {
//        args = new String[] {"C:\\other\\daneshgah\\phd-kth\\projects\\explanation generation\\tmp\\repos.txt",
//                "C:\\other\\daneshgah\\phd-kth\\projects\\explanation generation\\tmp\\repo",
//                "C:\\other\\daneshgah\\phd-kth\\projects\\explanation generation\\tmp\\output"};
        String reposPath = args[0];
        File cloneDir = new File(args[1]), outputDir = new File(args[2]);
        String resultsPath = args[2] + File.separator + "results.json";
        String mavenPath = args[3];

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

            try {
                ExplanationGenerator.getInstance(args[3]).runTestsAndSaveCloverReport(cloneDir.getPath(), cloverOutputPath);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            Boolean isCloverReportGenerated = new File(cloverOutputPath).exists();
            result.put(repo + ":::" + headCommitId, isCloverReportGenerated);

            PrintWriter pw = new PrintWriter(new FileWriter(outputDir.getPath() + File.separator
                    + "partial-result", true));
            pw.println(repo + " " + headCommitId + " " + isCloverReportGenerated);
            pw.flush();
            pw.close();
        }

        JsonObject root = new JsonObject();
        JsonArray results = new JsonArray();
        for(Map.Entry<String, Boolean> entry : result.entrySet()){
            JsonObject repoResult = new JsonObject();
            repoResult.addProperty("project-addr", entry.getKey().split(":::")[0]);
            repoResult.addProperty("commit-id", entry.getKey().split(":::")[1]);
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
