import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class InsecureXMLInputFactoryUsage {
    public static String parse(String xmlFile) throws FileNotFoundException, XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newInstance();  // Noncompliant
        XMLEventReader eventReader = factory.createXMLEventReader(new FileReader(xmlFile));
        return eventReader.getElementText();
    }

    public static String parseChained(String xmlFile) throws FileNotFoundException, XMLStreamException {
        return XMLInputFactory.newInstance().createXMLEventReader(new FileReader(xmlFile)).getElementText(); // Noncompliant
    }
}
