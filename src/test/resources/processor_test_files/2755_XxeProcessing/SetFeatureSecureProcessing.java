import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringWriter;

public class SetFeatureSecureProcessing {
    public static String transform(String xslt, String xml) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();  // Noncompliant
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Transformer transformer = transformerFactory.newTransformer(new StreamSource(xslt));

        StringWriter writer = new StringWriter();
        transformer.transform(new StreamSource(xml), new StreamResult(writer));
        return writer.toString();
    }

    public static Document parse(String xmlFile) throws Exception {
        DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();  // Noncompliant
        df.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder builder = df.newDocumentBuilder();
        return builder.parse(new InputSource(xmlFile));
    }
}
