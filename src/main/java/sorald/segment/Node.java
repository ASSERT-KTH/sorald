package sorald.segment;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Node {
    private Node parent;

    private int javaFilesNbs; // the number of java files in the current directory and all
    // subdirectories.

    private String rootPath;

    private LinkedList<Node> children;

    private List<String>
            javaFiles; // if its not a dir then it should contains a collection of java files,
    // should be
    // immutable after constructed by treealgorithm.

    private Node() {
        this.children = new LinkedList<Node>();
        this.javaFiles = new ArrayList<String>();
        this.javaFilesNbs = 0;
    }

    public Node(String rootPath) {
        this();
        this.rootPath = rootPath;
    }

    public Node(Node parent) {
        this();
        this.parent = parent;
    }

    public Node(Node parent, String rootPath) {
        this(rootPath);
        this.parent = parent;
    }

    // creation of a FileNode
    public Node(Node parent, List<String> javaFiles) {
        this(parent);
        this.javaFiles = javaFiles;
        this.javaFilesNbs = javaFiles.size();
    }

    public void updateJavaFileNbs(int newFoundJavaFiles) {
        this.javaFilesNbs += newFoundJavaFiles;
        if (parent != null) {
            parent.updateJavaFileNbs(newFoundJavaFiles);
        }
    }

    public int getJavaFilesNbs() {
        return this.javaFilesNbs;
    }

    public String getRootPath() {
        return this.rootPath;
    }

    public List<Node> getChildren() {
        return this.children;
    }

    public List<String> getJavaFiles() {
        return this.javaFiles;
    }

    public Node getParent() {
        return this.parent;
    }

    public boolean isRoot() {
        return this.parent == null;
    }

    public boolean isDirNode() {
        return !this.isFileNode();
    }

    public boolean isFileNode() {
        return this.children.isEmpty() && rootPath == null;
    }

    public void printTree() {
        LinkedList<Node> nodes4Traverse = new LinkedList<Node>();
        nodes4Traverse.add(this);
        while (!nodes4Traverse.isEmpty()) {
            Node node = nodes4Traverse.pop();
            if (node.getJavaFilesNbs() != 0) {
                if (node.isFileNode()) {
                    System.out.println(
                            "Files path: "
                                    + node.getParent().getRootPath()
                                    + " FilesNbs: "
                                    + node.getJavaFilesNbs());
                } else {
                    System.out.println(
                            "Dir path: "
                                    + node.getRootPath()
                                    + " FilesNbs: "
                                    + node.getJavaFilesNbs());
                }
            }
            nodes4Traverse.addAll(node.getChildren());
        }
    }
}
