package sorald.explanation.models;

import java.util.HashSet;
import java.util.Set;

public class CoverageDiff {
    private Set<Integer> newlyCoveredLines, newlyUncoveredLines,
            srcLinesCoveredInBothVersions, dstLinesCoveredInBothVersions;

    public CoverageDiff(){
        newlyCoveredLines = new HashSet<>();
        newlyUncoveredLines = new HashSet<>();
        srcLinesCoveredInBothVersions = new HashSet<>();
        dstLinesCoveredInBothVersions = new HashSet<>();
    }

    public Set<Integer> getNewlyUncoveredLines() {
        return newlyUncoveredLines;
    }

    public void setNewlyUncoveredLines(Set<Integer> newlyUncoveredLines) {
        this.newlyUncoveredLines = newlyUncoveredLines;
    }

    public Set<Integer> getNewlyCoveredLines() {
        return newlyCoveredLines;
    }

    public void setNewlyCoveredLines(Set<Integer> newlyCoveredLines) {
        this.newlyCoveredLines = newlyCoveredLines;
    }

    public Set<Integer> getDstLinesCoveredInBothVersions() {
        return dstLinesCoveredInBothVersions;
    }

    public void setDstLinesCoveredInBothVersions(Set<Integer> dstLinesCoveredInBothVersions) {
        this.dstLinesCoveredInBothVersions = dstLinesCoveredInBothVersions;
    }

    public Set<Integer> getSrcLinesCoveredInBothVersions() {
        return srcLinesCoveredInBothVersions;
    }

    public void setSrcLinesCoveredInBothVersions(Set<Integer> srcLinesCoveredInBothVersions) {
        this.srcLinesCoveredInBothVersions = srcLinesCoveredInBothVersions;
    }
}
