package sorald.explanation.models;

import gumtree.spoon.diff.operations.DeleteOperation;
import gumtree.spoon.diff.operations.InsertOperation;
import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.diff.operations.UpdateOperation;
import sorald.explanation.utils.GumtreeComparison;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ExecutionTraceChanges {
    private Set<Integer> linesOnlyInDst;
    private Set<Integer> linesOnlyInSrc;
    private Set<Integer> newlyCoveredLines;
    private Set<Integer> newlyUncoveredLines;
    private Set<Integer> srcLinesCoveredInBoth;
    private Set<Integer> dstLinesCoveredInBoth;

    public ExecutionTraceChanges
            (
                    CloverReportForFile oldReport,
                    CloverReportForFile newReport,
                    GumtreeComparison gtComparison
            ){
        linesOnlyInDst = new HashSet<>();
        linesOnlyInSrc = new HashSet<>();
        newlyCoveredLines = new HashSet<>();
        newlyUncoveredLines = new HashSet<>();

        for(Operation op : gtComparison.getOps()){
            if(op instanceof InsertOperation){
                linesOnlyInDst.addAll(IntStream.rangeClosed(op.getSrcNode().getPosition().getLine(),
                        op.getSrcNode().getPosition().getEndLine()).boxed().collect(Collectors.toList()));
            } else if(op instanceof DeleteOperation){
                linesOnlyInSrc.addAll(IntStream.rangeClosed(op.getSrcNode().getPosition().getLine(),
                        op.getSrcNode().getPosition().getEndLine()).boxed().collect(Collectors.toList()));
            } else if(op instanceof UpdateOperation){
                linesOnlyInSrc.addAll(IntStream.rangeClosed(op.getSrcNode().getPosition().getLine(),
                        op.getSrcNode().getPosition().getEndLine()).boxed().collect(Collectors.toList()));
                linesOnlyInDst.addAll(IntStream.rangeClosed(op.getDstNode().getPosition().getLine(),
                        op.getSrcNode().getPosition().getEndLine()).boxed().collect(Collectors.toList()));
            }
        }

        CoverageDiff changedCoverageHits = oldReport.coverageDiff(newReport, gtComparison.getSrcLineToDstLine(),
                gtComparison.getDstLineToSrcLine());
        for(Integer lineNum : changedCoverageHits.getNewlyCoveredLines()){
            if(linesOnlyInDst.contains(lineNum)){
                continue;
            }
            newlyCoveredLines.add(lineNum); // newly covered and not inserted/updated
        }

        for(Integer lineNum : changedCoverageHits.getNewlyUncoveredLines()){
            if(linesOnlyInSrc.contains(lineNum)){
                continue;
            }
            newlyUncoveredLines.add(lineNum); // newly uncovered and not deleted/updated
        }

        srcLinesCoveredInBoth = changedCoverageHits.getSrcLinesCoveredInBothVersions();
        dstLinesCoveredInBoth = changedCoverageHits.getDstLinesCoveredInBothVersions();
    }

    public Set<Integer> getLinesOnlyInDst() {
        return linesOnlyInDst;
    }

    public void setLinesOnlyInDst(Set<Integer> linesOnlyInDst) {
        this.linesOnlyInDst = linesOnlyInDst;
    }

    public Set<Integer> getLinesOnlyInSrc() {
        return linesOnlyInSrc;
    }

    public void setLinesOnlyInSrc(Set<Integer> linesOnlyInSrc) {
        this.linesOnlyInSrc = linesOnlyInSrc;
    }

    public Set<Integer> getNewlyCoveredLines() {
        return newlyCoveredLines;
    }

    public void setNewlyCoveredLines(Set<Integer> newlyCoveredLines) {
        this.newlyCoveredLines = newlyCoveredLines;
    }

    public Set<Integer> getNewlyUncoveredLines() {
        return newlyUncoveredLines;
    }

    public void setNewlyUncoveredLines(Set<Integer> newlyUncoveredLines) {
        this.newlyUncoveredLines = newlyUncoveredLines;
    }

    public Set<Integer> getSrcLinesCoveredInBoth() {
        return srcLinesCoveredInBoth;
    }

    public void setSrcLinesCoveredInBoth(Set<Integer> srcLinesCoveredInBoth) {
        this.srcLinesCoveredInBoth = srcLinesCoveredInBoth;
    }

    public Set<Integer> getDstLinesCoveredInBoth() {
        return dstLinesCoveredInBoth;
    }

    public void setDstLinesCoveredInBoth(Set<Integer> dstLinesCoveredInBoth) {
        this.dstLinesCoveredInBoth = dstLinesCoveredInBoth;
    }
}
