package mil.nga.mapcache.ogc.wms;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Parses the WMS getCapabilities xml and populates the WMSCapabilities object.
 */
public class CapabilitiesParser extends DefaultHandler {

    /**
     * The name of the title element.
     */
    private static String TITLE_ELEMENT = "Title";

    /**
     * The name of the name element.
     */
    private static String NAME_ELEMENT = "Name";

    /**
     * The name of the layer element.
     */
    private static String LAYER_ELEMENT = "Layer";

    /**
     * The name of the GetMap element.
     */
    private static String GETMAP_ELEMENT = "GetMap";

    /**
     * The name of the GetMap Format element.
     */
    private static String FORMAT_ELEMENT = "Format";

    /**
     * The name of the CRS element within the Layer element.
     */
    private static String CRS_ELEMENT = "CRS";

    /**
     * The current WMSCapabilties being populated.
     */
    private WMSCapabilities current = null;

    /**
     * The current layer we are parsing.
     */
    private Layer currentLayer = null;

    /**
     * The name of the current element.
     */
    private String currentElementName;

    /**
     * The current CRS string being built.
     */
    private String currentCRS = null;

    /**
     * The current stack of all parent layers.
     */
    private Stack<Layer> parentLayers = new Stack<>();

    /**
     * The parents elements we are currently parsing.
     */
    private Stack<String> currentElements = new Stack<>();

    /**
     * Parses the wms getCapabilities document and returns a new WMSCapabilities populated
     * with the data within the xml document.
     *
     * @param stream The input stream of the xml document.
     * @return A new WMSCapabilities object.
     */
    public WMSCapabilities parse(InputStream stream) throws ParserConfigurationException,
            SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        InputSource source = new InputSource();
        source.setByteStream(stream);
        parser.parse(source, this);

        return current;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        if (currentLayer != null) {
            if (TITLE_ELEMENT.equals(currentElementName) && currentLayer.getTitle().isEmpty()) {
                currentLayer.setTitle(new String(ch, start, length));
            } else if (NAME_ELEMENT.equals(currentElementName) && currentLayer.getName().isEmpty()) {
                currentLayer.setName(new String(ch, start, length));
            }
        }

        if (FORMAT_ELEMENT.equals(currentElementName)) {
            if (currentElements.lastElement().equals(GETMAP_ELEMENT)) {
                current.getCapability().getRequest().getGetMap().getFormat().add(
                        new String(ch, start, length));
            }
        } else if (CRS_ELEMENT.equals(currentElementName)) {
            if(currentCRS == null) {
                currentCRS = "";
            }
            String crs = new String(ch, start, length);
            currentCRS += crs;
        }
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        current = new WMSCapabilities();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        currentElements.push(currentElementName);
        currentElementName = localName;
        if (localName.equals(LAYER_ELEMENT)) {
            handleLayer();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        currentElementName = currentElements.pop();
        if (localName.equals(LAYER_ELEMENT)) {
            if (parentLayers.empty()) {
                currentLayer = null;
            } else {
                currentLayer = parentLayers.pop();
            }
        } else if(CRS_ELEMENT.equals(localName)) {
            currentLayer.getCRS().add(currentCRS);
            currentCRS = null;
        }
    }

    /**
     * Handles when we reach a new layer element.
     */
    private void handleLayer() {
        if (currentLayer == null) {
            currentLayer = new Layer();
            current.getCapability().getLayer().add(currentLayer);
        } else {
            Layer childLayer = new Layer();
            currentLayer.getLayers().add(childLayer);
            parentLayers.push(currentLayer);
            currentLayer = childLayer;
        }
    }
}