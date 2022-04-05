import javax.xml.XMLConstants;
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
}
