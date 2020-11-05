import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class DocumentBuilderChainedLocalVariable {
    public static Document parse(String xmlFile) throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();  // Noncompliant
        return builder.parse(new InputSource(xmlFile));
    }
}
