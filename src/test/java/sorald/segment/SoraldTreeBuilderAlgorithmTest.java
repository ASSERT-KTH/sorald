package sorald.segment;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sorald.TestHelper;

public class SoraldTreeBuilderAlgorithmTest {

    @Test
    public void treeBuildTest() throws IOException {
        Path folder = TestHelper.createTemporaryTestResourceWorkspace().resolve("DummyTreeDir");
        Node rootNode = SoraldTreeBuilderAlgorithm.buildTree(folder.toString());
        File rootFolder = folder.toFile();
        Assertions.assertEquals("DummyTreeDir", rootFolder.getName());
        Assertions.assertEquals(3, rootNode.getJavaFilesNbs());
        Assertions.assertEquals(2, rootNode.getChildren().size());

        Node rootFileNode = rootNode.getChildren().get(1);
        Assertions.assertTrue(rootFileNode.isFileNode());
        Assertions.assertEquals(1, rootFileNode.getJavaFiles().size());
        File file1 = new File(rootFileNode.getJavaFiles().get(0));
        Assertions.assertEquals("DummyOne.java", file1.getName());

        Node subDirNode = rootNode.getChildren().get(0);
        File subDirFolder = new File(subDirNode.getRootPath());
        Assertions.assertTrue(subDirNode.isDirNode());
        Assertions.assertEquals("DummySubFolder", subDirFolder.getName());
        Assertions.assertEquals(2, subDirNode.getJavaFilesNbs());
        Assertions.assertEquals(1, subDirNode.getChildren().size());

        Node subDirFileNode = subDirNode.getChildren().get(0);
        File file2 = new File(subDirFileNode.getJavaFiles().get(0));
        File file3 = new File(subDirFileNode.getJavaFiles().get(1));
        List<String> dummyFileNames =
                subDirFileNode.getJavaFiles().stream()
                        .map(absolutPath -> new File(absolutPath).getName())
                        .collect(Collectors.toList());
        MatcherAssert.assertThat(
                dummyFileNames, containsInAnyOrder("DummyTwo.java", "DummyThree.java"));
    }
}
