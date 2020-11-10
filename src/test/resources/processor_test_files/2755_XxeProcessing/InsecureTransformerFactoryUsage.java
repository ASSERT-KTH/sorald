import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringWriter;

public class InsecureTransformerFactoryUsage {
    public static String transform(String xslt, String xml) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();  // Noncompliant
        Transformer transformer = transformerFactory.newTransformer(new StreamSource(xslt));

        StringWriter writer = new StringWriter();
        transformer.transform(new StreamSource(xml), new StreamResult(writer));
        return writer.toString();
    }
}
