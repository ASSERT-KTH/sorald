package sorald.explanation.models;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.*;

public class CloverReportForFile {
    private String fileName;
    private Map<Integer, Integer> hitsPerLines;
    private DocumentBuilder db;
    private Node fileNode;

    public CloverReportForFile(String reportPath, String changeFilePath)
            throws ParserConfigurationException, IOException, SAXException {
        hitsPerLines = new HashMap<>();

        db = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        Document doc = db.parse(reportPath);
        doc.normalize();


        NodeList fileNodes = doc.getElementsByTagName("file");
        for(int i = 0; i < fileNodes.getLength(); i++){
            Node node = fileNodes.item(i);

            NamedNodeMap attrs = node.getAttributes();
            if(attrs.getNamedItem("path").getNodeValue().equals(changeFilePath)){
                fileNode = node;
                break;
            }
        }

        if(fileNode == null)
            throw new NoSuchFileException("the mentioned changedFile does not exist: " + changeFilePath);

        NodeList lineNodes = fileNode.getChildNodes();
        for(int i = 0; i < lineNodes.getLength(); i++){
            Node node = lineNodes.item(i);

            if(node.getNodeName().equals("line")){
                NamedNodeMap attrs = node.getAttributes();
                int lineNumber = Integer.parseInt(attrs.getNamedItem("num").getNodeValue()), hitsCnt = -1;

                if(attrs.getNamedItem("type").getNodeValue().equals("cond")){
                    hitsCnt = Integer.parseInt(attrs.getNamedItem("truecount").getNodeValue()) +
                            Integer.parseInt(attrs.getNamedItem("falsecount").getNodeValue());
                }else {
                    hitsCnt = Integer.parseInt(attrs.getNamedItem("count").getNodeValue());
                }

                hitsPerLines.put(lineNumber, hitsCnt);
            }
        }
    }

    public CoverageDiff coverageDiff
            (
                    CloverReportForFile otherReport,
                    Map<Integer, Integer> srcLinesToDstLines,
                    Map<Integer, Integer> dstLinesToSrcLines
            ){
        CoverageDiff diff = new CoverageDiff();

        for(Map.Entry<Integer, Integer> hitForLine : hitsPerLines.entrySet()){
            int lineNum = hitForLine.getKey(), hitsCnt = hitForLine.getValue();

            int correspondingLineNum = srcLinesToDstLines.containsKey(lineNum) ?
                    srcLinesToDstLines.get(lineNum) : -1;

            if((!otherReport.getHitsPerLines().containsKey(correspondingLineNum)
                    || otherReport.getHitsPerLines().get(correspondingLineNum) == 0) && hitsCnt > 0){
                    diff.getNewlyUncoveredLines().add(lineNum); // newly uncovered or deleted
            }

            if(otherReport.getHitsPerLines().containsKey(correspondingLineNum)
                    && otherReport.getHitsPerLines().get(correspondingLineNum) > 0 && hitsCnt > 0){
                diff.getSrcLinesCoveredInBothVersions().add(lineNum);
            }
        }

        for(Map.Entry<Integer, Integer> hitForLine : otherReport.getHitsPerLines().entrySet()){
            int lineNum = hitForLine.getKey(), hitsCnt = hitForLine.getValue();

            int correspondingLineNum = dstLinesToSrcLines.containsKey(lineNum) ?
                    dstLinesToSrcLines.get(lineNum) : -1;

            if((!hitsPerLines.containsKey(correspondingLineNum) || hitsPerLines.get(correspondingLineNum) == 0)
                    && hitsCnt > 0){
                    diff.getNewlyCoveredLines().add(lineNum); // newly covered or inserted
            }

            if(hitsPerLines.containsKey(correspondingLineNum) && hitsPerLines.get(correspondingLineNum) > 0
                    && hitsCnt > 0){
                diff.getDstLinesCoveredInBothVersions().add(lineNum);
            }
        }

        return diff;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Map<Integer, Integer> getHitsPerLines() {
        return hitsPerLines;
    }

    public void setHitsPerLines(Map<Integer, Integer> hitsPerLines) {
        this.hitsPerLines = hitsPerLines;
    }

    public Node getFileNode() {
        return fileNode;
    }

    public void setFileNode(Node fileNode) {
        this.fileNode = fileNode;
    }
}
