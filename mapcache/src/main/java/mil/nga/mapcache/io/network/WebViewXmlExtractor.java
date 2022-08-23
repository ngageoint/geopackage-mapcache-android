package mil.nga.mapcache.io.network;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Extracts the xml from within the web page.
 */
public class WebViewXmlExtractor implements WebViewExtractor {

    @Override
    public InputStream extractContent(String html) {
        int newLineIndex = html.indexOf("\\n\\n") + 4;
        String xml = html.substring(newLineIndex, html.length() - 1);
        xml = xml.replace("\\u003C", "<");
        xml = xml.replace("\\\"", "\"");
        xml = xml.replace("\\n", "");

        return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
    }
}
