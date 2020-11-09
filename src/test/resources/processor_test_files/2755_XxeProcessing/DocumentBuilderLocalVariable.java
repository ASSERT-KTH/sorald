import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class DocumentBuilderLocalVariable {
    public static Document parse(String xmlFile) throws Exception {
        DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();  // Noncompliant
        DocumentBuilder builder = df.newDocumentBuilder();
        return builder.parse(new InputSource(xmlFile));
    }
}
