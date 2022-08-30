package mil.nga.mapcache.io.network;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import java.io.InputStream;
import java.util.Observable;
import java.util.Observer;

import mil.nga.mapcache.utils.ThreadUtils;

/**
 * Class that is called from within the web view page.
 */
public class WebViewContentRetriever implements Observer, ValueCallback<String> {

    /**
     * The javascript that returns the urls html content.
     */
    private static final String theJavaScript = "(function() { return "
            + "(document.getElementsByTagName('html')[0].innerText); })();";

    /**
     * Contains the current url and html.
     */
    private final WebViewRequestModel model;

    /**
     * The web view being used to load urls.
     */
    private final WebView webView;

    /**
     * Creates extractors to be used on the web view and grab certain contents within it.
     */
    private final WebViewExtractorFactory extractorFactory;

    /**
     * Used to run background tasks back on the UI thread.
     */
    private final Activity activity;

    /**
     * The html that goes with the extractor.
     */
    private String currentHtml;

    /**
     * The current extractor needing execution.
     */
    private WebViewExtractor currentExtractor;

    /**
     * Constructor.
     *
     * @param activity Used to run background tasks back on the UI thread.
     * @param webView  The web view to get the content from.
     * @param model    Contains the current url and html.
     */
    @SuppressLint("SetJavaScriptEnabled")
    public WebViewContentRetriever(Activity activity, WebView webView, WebViewRequestModel model) {
        this.activity = activity;
        this.webView = webView;
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.model = model;
        extractorFactory = new WebViewExtractorFactory(this.webView, this.model);
        this.model.addObserver(this);
    }

    @Override
    public void update(Observable observable, Object o) {
        if (WebViewRequestModel.CURRENT_URL_PROP.equals(o)) {
            this.webView.evaluateJavascript(theJavaScript, this);
        }
    }

    /**
     * Stops getting the html for urls.
     */
    public void close() {
        this.model.deleteObserver(this);
    }

    @Override
    public void onReceiveValue(String html) {
        WebViewExtractor extractor = extractorFactory.createExtractor(html);
        synchronized (this) {
            currentExtractor = extractor;
            currentHtml = html;
        }

        if (extractor != null) {
            ThreadUtils.getInstance().runBackground(this::waitBackBeforeExtract);
        }
    }

    /**
     * Waits in a background thread for the web page to fully load before extracting its content.
     */
    private void waitBackBeforeExtract() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Log.d(WebViewContentRetriever.class.getSimpleName(), e.getMessage(), e);
        }

        this.activity.runOnUiThread(this::extractContent);
    }

    /**
     * Extracts the contents from the web page.
     */
    private void extractContent() {
        synchronized (this) {
            if (currentExtractor != null) {
                if (currentExtractor.readyForExtraction(currentHtml)) {
                    InputStream content = currentExtractor.extractContent(currentHtml);
                    if (content != null) {
                        this.model.setCurrentContent(content);
                    }
                } else {
                    ThreadUtils.getInstance().runBackground(this::waitBackBeforeExtract);
                }
            }
        }
    }
}
