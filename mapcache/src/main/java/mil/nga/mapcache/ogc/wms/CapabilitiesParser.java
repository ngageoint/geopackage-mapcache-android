package mil.nga.mapcache.ogc.wms;

import android.renderscript.ScriptGroup;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Parses the WMS getCapabilities xml and populates the WMSCapabilities object.
 */
public class CapabilitiesParser extends DefaultHandler {

    /**
     * The current WMSCapabilties being populated.
     */
    private WMSCapabilities current = null;

    /**
     * The current layer we are parsing.
     */
    private Layer currentLayer = null;

    /**
     * The current stack of all parent layers.
     */
    private Stack<Layer> parentLayers = new Stack<>();

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
    public void startDocument() throws SAXException {
        super.startDocument();
        current = new WMSCapabilities();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (localName.equals("Layer")) {
            handleLayer();
        } else if (currentLayer != null && localName.equals("Title")) {
            currentLayer.setTitle(attributes.toString());
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        if (localName.equals("Layer")) {
            if (parentLayers.empty()) {
                currentLayer = null;
            } else {
                currentLayer = parentLayers.pop();
            }
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
