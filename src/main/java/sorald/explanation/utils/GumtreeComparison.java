package sorald.explanation.utils;

import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.ITree;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.ActionClassifier;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GumtreeComparison {
    private Diff diff;
    private List<Operation> ops;
    private File oldSource, newSource;
    private Map<Integer, Integer> srcLineToDstLine, dstLineToSrcLine;

    public GumtreeComparison(String oldSourcePath, String newSourcePath) throws Exception {
        computeDiff(oldSourcePath, newSourcePath);

        computeLineMappings();
    }

    private void computeLineMappings() {
        srcLineToDstLine = new HashMap<>();
        dstLineToSrcLine = new HashMap<>();

        Map<Integer, CtElement> srcLineToElem = new HashMap<>(),
                dstLineToElem = new HashMap<>();

        MappingStore treeMappings = diff.getMappingsComp();

        Iterator<Mapping> mappingIt = treeMappings.iterator();
        while (mappingIt.hasNext()) {
            Mapping mapping = mappingIt.next();

            CtElement src = (CtElement) mapping.getFirst().getMetadata("spoon_object"),
                    dst = (CtElement) mapping.getSecond().getMetadata("spoon_object");

            if (src != null && !src.getPosition().equals(SourcePosition.NOPOSITION)) {
                for (int i = src.getPosition().getLine(); i <= src.getPosition().getEndLine(); i++) {
                    int elemLineCnt = src.getPosition().getEndLine() - src.getPosition().getLine();

                    SourcePosition mappedPos = srcLineToElem.containsKey(i) ? srcLineToElem.get(i).getPosition() : null;

                    if (mappedPos == null ||
                            mappedPos.getEndLine() - mappedPos.getLine() > elemLineCnt) {
                        srcLineToElem.put(i, src);
                    }
                }
            }

            if (dst != null && !dst.getPosition().equals(SourcePosition.NOPOSITION)) {
                for (int i = dst.getPosition().getLine(); i <= dst.getPosition().getEndLine(); i++) {
                    int elemLineCnt = dst.getPosition().getEndLine() - dst.getPosition().getLine();

                    SourcePosition mappedPos = dstLineToElem.containsKey(i) ? dstLineToElem.get(i).getPosition() : null;

                    if (mappedPos == null ||
                            mappedPos.getEndLine() - mappedPos.getLine() > elemLineCnt) {
                        dstLineToElem.put(i, dst);
                    }
                }
            }

        }

        for (Map.Entry<Integer, CtElement> entry : srcLineToElem.entrySet()) {
            Integer srcLine = entry.getKey();
            CtElement srcElem = entry.getValue();
            ITree srcTree = (ITree) srcElem.getMetadata("gtnode");
            CtElement dstElem = (CtElement) treeMappings.getDst(srcTree).getMetadata("spoon_object");
            Integer dstStartLine = dstElem.getPosition().getLine();

            srcLineToDstLine.put(srcLine, dstStartLine);
        }

        for (Map.Entry<Integer, CtElement> entry : dstLineToElem.entrySet()) {
            Integer dstLine = entry.getKey();
            CtElement dstElem = entry.getValue();
            ITree dstTree = (ITree)dstElem.getMetadata("gtnode");
            CtElement srcElem = (CtElement)treeMappings.getSrc(dstTree).getMetadata("spoon_object");
            Integer srcStartLine = srcElem.getPosition().getLine();

            dstLineToSrcLine.put(dstLine, srcStartLine);
        }
    }

    private void computeDiff(String oldSourcePath, String newSourcePath) throws Exception {
        oldSource = new File(oldSourcePath);
        newSource = new File(newSourcePath);

        diff = new AstComparator().compare(oldSource, newSource);
        ops = ActionClassifier.replaceMoveFromRoots(diff);
    }

    public Diff getDiff() {
        return diff;
    }

    public void setDiff(Diff diff) {
        this.diff = diff;
    }

    public List<Operation> getOps() {
        return ops;
    }

    public void setOps(List<Operation> ops) {
        this.ops = ops;
    }

    public File getNewSource() {
        return newSource;
    }

    public void setNewSource(File newSource) {
        this.newSource = newSource;
    }

    public File getOldSource() {
        return oldSource;
    }

    public void setOldSource(File oldSource) {
        this.oldSource = oldSource;
    }

    public Map<Integer, Integer> getDstLineToSrcLine() {
        return dstLineToSrcLine;
    }

    public void setDstLineToSrcLine(Map<Integer, Integer> dstLineToSrcLine) {
        this.dstLineToSrcLine = dstLineToSrcLine;
    }

    public Map<Integer, Integer> getSrcLineToDstLine() {
        return srcLineToDstLine;
    }

    public void setSrcLineToDstLine(Map<Integer, Integer> srcLineToDstLine) {
        this.srcLineToDstLine = srcLineToDstLine;
    }
}
