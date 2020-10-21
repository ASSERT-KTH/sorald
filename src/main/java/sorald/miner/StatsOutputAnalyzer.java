package sorald.miner;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import sorald.Constants;

public class StatsOutputAnalyzer {
    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(new File(Constants.PATH_TO_STATS_OUTPUT));

        Map<String, Integer> statsPerWarningType = new HashMap<>();
        int warningsOfCurRepo = 0, totalRepos = 0, totalWarnings = 0, maxRepoWarnings = 0;
        String repoWithMaxWarnings = null, curRepo = null;

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            if (line.contains("=")) { // a line like: "CastArithmeticOperandCheck=48"
                String warningType = line.split("=")[0];
                int warningCount = Integer.parseInt(line.split("=")[1]);
                warningsOfCurRepo += warningCount;
                totalWarnings += warningCount;

                if (statsPerWarningType.containsKey(warningType)) {
                    statsPerWarningType.put(
                            warningType, statsPerWarningType.get(warningType) + warningCount);
                } else {
                    statsPerWarningType.put(warningType, warningCount);
                }
            } else if (line.startsWith("RepoName: ")) { // a line like: RepoName: discover
                if (warningsOfCurRepo > maxRepoWarnings) {
                    maxRepoWarnings = warningsOfCurRepo;
                    repoWithMaxWarnings = curRepo;
                }

                curRepo = line.split(": ")[1];

                if (!line.contains("not_cloned")) // a line like: RepoName: discover not_cloned
                totalRepos++;

                warningsOfCurRepo = 0;
            }
        }

        sc.close();

        System.out.println("TotalRepos: " + totalRepos);
        System.out.println("TotalWarnings: " + totalWarnings);
        System.out.println("AVGWarnings: " + (totalWarnings / totalRepos));
        System.out.println("RepoWithMaxWarnings: " + repoWithMaxWarnings);
        System.out.println("MaxRepoWarnings: " + maxRepoWarnings);

        statsPerWarningType.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .forEach(System.out::println);
    }
}
