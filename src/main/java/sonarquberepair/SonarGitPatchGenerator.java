package sonarquberepair;

import java.io.PrintWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;

import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.HistogramDiff;
import org.eclipse.jgit.diff.RawTextComparator;


public class SonarGitPatchGenerator {
	private String gitProjectRootdir;

	public SonarGitPatchGenerator() {
		this.gitProjectRootdir = ".";
	}

	public SonarGitPatchGenerator(String gitProjectRootdir) {
		this.gitProjectRootdir = gitProjectRootdir;
	}
	
	public void setGitProjectRootDir(String gitProjectRootdir) {
		this.gitProjectRootdir = gitProjectRootdir;
	}

	public String getGitProjectRootDir() {
		return this.gitProjectRootdir;
	}

	public void generate(String originalFilePath, String newFilePath,String pathToPatch) {

        try {

            FileOutputStream out = new FileOutputStream(pathToPatch + ".patch");

            PrintWriter printer = new PrintWriter(out);
            String relativeOriginalFilePath = new File(this.gitProjectRootdir).toURI().relativize(new File(originalFilePath).toURI()).getPath();

            out.write(("--- a/" +  relativeOriginalFilePath + "\n").getBytes());
            out.write(("+++ b/" +  relativeOriginalFilePath + "\n").getBytes());
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