package sorald.segment;

import java.io.File;
import sorald.Constants;

public class SoraldTreeBuilderAlgorithm {

    public static Node buildTree(String dirPathAbsolutePath) {
        Node startNode = new Node(dirPathAbsolutePath);
        File file = new File(startNode.getRootPath());
        if (file.isFile()) {
            startNode.getJavaFiles().add(file.getAbsolutePath());
        } else {
            construct4CurrentNode(startNode);
        }
        return startNode;
    }

    // return list of dir nodes and a files node from current dir node.
    private static void construct4CurrentNode(Node node) {
        if (node.isDirNode()) {
            File currentDir = new File(node.getRootPath());
            File[] filesList = currentDir.listFiles();
            Node fileNode = new Node(node);
            for (File f : filesList) {
                if (f.isDirectory()) {
                    node.getChildren().add(new Node(node, f.getAbsolutePath()));
                } else if (f.isFile() && f.getPath().endsWith(Constants.JAVA_EXT)) {
                    fileNode.getJavaFiles().add(f.getAbsolutePath());
                }
            }
            if (fileNode.getJavaFiles().size() != 0) {
                node.getChildren().add(fileNode);
            }
        }
        if (node.getJavaFiles().size() != 0) {
            node.updateJavaFileNbs(node.getJavaFiles().size());
        }
        for (Node child : node.getChildren()) {
            construct4CurrentNode(child);
        }
    }
}
