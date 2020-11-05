package sorald;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.HistogramDiff;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;

public class GitPatchGenerator {
    private String gitProjectRootDir;

    public GitPatchGenerator() {
        this.gitProjectRootDir = ".";
    }

    public GitPatchGenerator(String gitProjectRootDir) {
        this.gitProjectRootDir = gitProjectRootDir;
    }

    public void setGitProjectRootDir(String gitProjectRootDir) {
        this.gitProjectRootDir = gitProjectRootDir;
    }

    public String getGitProjectRootDir() {
        return this.gitProjectRootDir;
    }

    public void generate(String originalFilePath, String newFilePath, String pathToPatch) {

        try {

            FileOutputStream out = new FileOutputStream(pathToPatch + Constants.PATCH_EXT);

            PrintWriter printer = new PrintWriter(out);
            String relativeOriginalFilePath =
                    new File(this.gitProjectRootDir)
                            .toURI()
                            .relativize(new File(originalFilePath).toURI())
                            .getPath();

            out.write(("--- a/" + relativeOriginalFilePath + "\n").getBytes());
            out.write(("+++ b/" + relativeOriginalFilePath + "\n").getBytes());
            RawText rt1 = new RawText(new File(originalFilePath));
            RawText rt2 = new RawText(new File(newFilePath));
            EditList diffList = new EditList();
            diffList.addAll(new HistogramDiff().diff(RawTextComparator.DEFAULT, rt1, rt2));
            DiffFormatter diffForm = new DiffFormatter(out);
            diffForm.format(diffList, rt1, rt2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
