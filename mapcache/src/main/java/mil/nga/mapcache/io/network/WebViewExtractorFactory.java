package mil.nga.mapcache.io.network;

import android.webkit.WebView;

/**
 * Based on the html content this class will return the appropriate WebViewExtractor.
 */
public class WebViewExtractorFactory {

    /**
     * The web view to get the image from.
     */
    private final WebView webView;

    /**
     * Keeps track of the current url.
     */
    private final WebViewRequestModel model;

    /**
     * Constructor.
     *
     * @param webView The web view to get the image from.
     * @param model   Keeps track of the current url.
     */
    public WebViewExtractorFactory(WebView webView, WebViewRequestModel model) {
        this.webView = webView;
        this.model = model;
    }

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
        } else if (html.contains("\"\"") && this.model.getCurrentUrl().contains("format=image")) {
            extractor = new WebViewImageExtractor(this.webView);
        }

        return extractor;
    }
}
