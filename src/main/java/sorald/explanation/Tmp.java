package sorald.explanation;

import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.ActionClassifier;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import org.eclipse.jgit.diff.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class Tmp {
    public static void main(String[] args) throws Exception {
        String sPath = "C:\\other\\daneshgah\\phd-kth\\" +
                "projects\\explanation generation\\tmp\\temp\\GetClassLoader.java",
                tPath = "C:\\other\\daneshgah\\phd-kth\\" +
                        "projects\\explanation generation\\tmp\\temp\\GetClassLoader-fixed-and-printed-normally.java";

        Diff diff = new AstComparator().compare(new File(sPath), new File(tPath));
        List<Operation> opsWithoutMove = ActionClassifier.replaceMoveFromRoots(diff);
    }

    private static String getDiff(String file1, String file2) {
        OutputStream out = new ByteArrayOutputStream();
        try {
            RawText rt1 = new RawText(new File(file1));
            RawText rt2 = new RawText(new File(file2));
            EditList diffList = new EditList();
            diffList.addAll(new HistogramDiff().diff(RawTextComparator.DEFAULT, rt1, rt2));
            new DiffFormatter(out).format(diffList, rt1, rt2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toString();
    }
}
