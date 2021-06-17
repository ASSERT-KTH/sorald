import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CollectionsEmptyConstants {
    public static void main(String[] args) {
        List<String> collection1 = Collections.EMPTY_LIST;  // Noncompliant
        Map<String, String> collection2 = Collections.EMPTY_MAP;  // Noncompliant
        Set<String> collection3 = Collections.EMPTY_SET;  // Noncompliant
        List<Integer> list = java.util.Collections.EMPTY_LIST; // Compliant due to false positive
    }
}
