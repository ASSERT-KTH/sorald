package sorald.segment;

import java.util.LinkedList;
import java.util.List;

public class FirstFitSegmentationAlgorithm {

    public static LinkedList<LinkedList<Node>> segment(Node startNode, int maxFiles) {
        LinkedList<Node> resources4Repair = new LinkedList<Node>();
        resources4Repair.add(startNode);
        return segmentHelper(resources4Repair, maxFiles);
    }

    public static LinkedList<LinkedList<Node>> segment(List<Node> oldSegment, int maxFiles) {
        LinkedList<Node> resources4Repair = new LinkedList<Node>();
        resources4Repair.addAll(oldSegment);
        return segmentHelper(resources4Repair, maxFiles);
    }

    private static LinkedList<LinkedList<Node>> segmentHelper(
            LinkedList<Node> resources4Repair, int maxFiles) {
        LinkedList<LinkedList<Node>> segments = new LinkedList<LinkedList<Node>>();
        LinkedList<Node> segment = new LinkedList<Node>();
        int currentAddedFiles = 0;
        while (!resources4Repair.isEmpty()) {
            Node node = resources4Repair.pop();
            if (currentAddedFiles + node.getJavaFilesNbs() > maxFiles) {
                // keep splitting dir node if does not fit until not-splittable
                if (node.isDirNode() && node.getChildren().size() != 0) {
                    resources4Repair.addAll(0, node.getChildren());
                } else {
                    // it's a file node , need to be splitted.
                    int index = maxFiles - currentAddedFiles;
                    Pair<Node, Node> splitted = splitFileNode(node, index);
                    segment.add(splitted.getFirst());
                    resources4Repair.addFirst(splitted.getSecond());
                    segments.add(segment);
                    currentAddedFiles = 0;
                    segment = new LinkedList<Node>();
                }
            } else {
                segment.add(node);
                currentAddedFiles += node.getJavaFilesNbs();
            }
            if (resources4Repair.isEmpty() || maxFiles - currentAddedFiles == 0) {
                segments.add(segment);
                currentAddedFiles = 0;
                segment = new LinkedList<Node>();
            }
        }
        return segments;
    }

    public static Pair<Node, Node> splitFileNode(Node fileNode, int index) {
        if (!(index > 0 || index < fileNode.getJavaFiles().size())) {
            System.out.println(
                    "Invalid index: " + index + " Files: " + fileNode.getJavaFiles().size());
            return null;
        }

        if (!fileNode.isFileNode()) {
            return null;
        }

        List<String> subliOne = fileNode.getJavaFiles().subList(0, index);
        List<String> subliTwo =
                fileNode.getJavaFiles().subList(index, fileNode.getJavaFiles().size());

        Node n1 = null;
        if (!subliOne.isEmpty()) {
            n1 = new Node(fileNode.getParent(), subliOne);
        }

        Node n2 = null;
        if (!subliTwo.isEmpty()) {
            n2 = new Node(fileNode.getParent(), subliTwo);
        }
        return new Pair<Node, Node>(n1, n2);
    }
}
