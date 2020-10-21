package sorald.segment;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sorald.Constants;

public class FirstFitSegmentationAlgorithmTest {

    @Test
    public void nodeSplitTest() throws Exception {
        List<String> filesPath = new ArrayList<String>();
        filesPath.add(".");
        filesPath.add(".");
        Node node = new Node(null, filesPath);
        Pair<Node, Node> p = FirstFitSegmentationAlgorithm.splitFileNode(node, 1);
        Assertions.assertEquals(1, p.getFirst().getJavaFiles().size());
        Assertions.assertEquals(1, p.getSecond().getJavaFiles().size());
    }

    @Test
    public void segmentationTest() throws Exception {
        String folder = Constants.PATH_TO_RESOURCES_FOLDER + "DummyTreeDir";
        Node rootNode = SoraldTreeBuilderAlgorithm.buildTree(folder);

        LinkedList<LinkedList<Node>> segments = FirstFitSegmentationAlgorithm.segment(rootNode, 2);
        Assertions.assertEquals(2, segments.size());

        LinkedList<Node> segmentOne = segments.get(0);
        Node dirNode = segmentOne.get(0);
        Node fileNodeOne = dirNode.getChildren().get(0);
        File subDirFolder = new File(dirNode.getRootPath());
        Assertions.assertTrue(dirNode.isDirNode());
        Assertions.assertTrue(fileNodeOne.isFileNode());
        Assertions.assertEquals(1, segmentOne.size());
        Assertions.assertEquals(1, dirNode.getChildren().size());
        Assertions.assertEquals(2, fileNodeOne.getJavaFiles().size());
        Assertions.assertEquals("DummySubFolder", subDirFolder.getName());
        List<String> dummyFileNames =
                fileNodeOne.getJavaFiles().stream()
                        .map(absolutePath -> new File(absolutePath).getName())
                        .collect(Collectors.toList());
        MatcherAssert.assertThat(
                dummyFileNames, containsInAnyOrder("DummyTwo.java", "DummyThree.java"));

        LinkedList<Node> segmentTwo = segments.get(1);
        Node fileNodeTwo = segmentTwo.get(0);
        File dummyOne = new File(fileNodeTwo.getJavaFiles().get(0));
        Assertions.assertTrue(fileNodeTwo.isFileNode());
        Assertions.assertEquals(1, segmentTwo.size());
        Assertions.assertEquals(1, fileNodeTwo.getJavaFiles().size());
        Assertions.assertEquals("DummyOne.java", dummyOne.getName());
    }
}
