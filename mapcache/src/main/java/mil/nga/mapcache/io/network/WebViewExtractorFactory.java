package mil.nga.mapcache.io.network;

import android.util.Log;

/**
 * Based on the html content this class will return the appropriate WebViewExtractor.
 */
public class WebViewExtractorFactory {

    /**
     * Creates the appropriate extractor based on the html content.
     *
     * @param html The html to inspect.
     * @return The extractor or null if it couldn't find one.
     */
    public WebViewExtractor createExtractor(String html) {

        WebViewExtractor extractor = null;

        if (html.startsWith("\"This XML file")) {
            extractor = new WebViewXmlExtractor();
        } else if (html.contains("image src")) {
            Log.d(WebViewExtractorFactory.class.getSimpleName(), "Image extractor");
        }

        return extractor;
    }
}
