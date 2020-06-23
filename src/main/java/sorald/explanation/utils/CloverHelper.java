package sorald.explanation.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

public class CloverHelper {
    private static final String CLOVER_PLUGIN_DEFINITION_FILENAME = "clover_plugin_nod.xml";

    private static CloverHelper _instance;

    private DocumentBuilder documentBuilder;

    public static CloverHelper getInstance() throws ParserConfigurationException {
        if (_instance == null)
            _instance = new CloverHelper();

        return _instance;
    }

    public CloverHelper() throws ParserConfigurationException {
        documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }

    public void addCloverPluginToPom(File inputPomFile, File outputPomFile)
            throws Exception {

        Document doc = documentBuilder.parse(inputPomFile);
        doc.normalize();

        NodeList buildNodes = doc.getElementsByTagName("build");

        Node buildNode = null;

        if (buildNodes.getLength() == 0) {
            buildNode = doc.createElement("build");
            doc.appendChild(buildNode);
        } else
            buildNode = buildNodes.item(0);

        NodeList buildChildNodes = buildNode.getChildNodes();

        Node pluginsNode = null;

        for (int i = 0; i < buildChildNodes.getLength(); i++) {
            if (buildChildNodes.item(i).getNodeName().equals("plugins")) {
                pluginsNode = buildChildNodes.item(i);
                break;
            }
        }

        if (pluginsNode == null) {
            pluginsNode = doc.createElement("plugins");
            buildNode.appendChild(pluginsNode);
        }

        boolean cloverPluginExists = false;
        NodeList pluginsChildNodes = pluginsNode.getChildNodes();
        for(int i = 0; i < pluginsChildNodes.getLength(); i++){
            if(pluginsChildNodes.item(i).getNodeName().equals("plugin")){
                NodeList pluginChildNodes = pluginsChildNodes.item(i).getChildNodes();
                for(int j = 0; j < pluginChildNodes.getLength(); j++){
                    if(pluginChildNodes.item(j).getNodeName().equals("artifactId")
                        && pluginChildNodes.item(j).getTextContent().equals("clover-maven-plugin")){
                        cloverPluginExists = true;
                        break;
                    }
                }
                if(cloverPluginExists)
                    break;
            }
        }
        if(cloverPluginExists)
            return;

        File cloverPluginFile =
                new File(getClass().getClassLoader().getResource(CLOVER_PLUGIN_DEFINITION_FILENAME).toURI());
        Node cloverTag = documentBuilder.parse(cloverPluginFile).getDocumentElement();
        cloverTag = doc.importNode(cloverTag, true);
        pluginsNode.appendChild(cloverTag);

        printDocument(doc, new FileOutputStream(outputPomFile));
    }

    public void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        transformer.transform(new DOMSource(doc),
                new StreamResult(new OutputStreamWriter(out, "UTF-8")));
    }
}
