package mil.nga.mapcache.io.network;

import java.io.InputStream;
import java.util.Observable;

/**
 * The model used by the WebViewRequest classes.
 */
public class WebViewRequestModel extends Observable {

    /**
     * The current url property.
     */
    public static String CURRENT_URL_PROP = "currentUrl";

    /**
     * The current html property.
     */
    public static String CURRENT_CONTENT_PROP = "currentContent";

    /**
     * The current url that the web view is at.
     */
    private String currentUrl;

    /**
     * The current content displayed on the web view page.
     */
    private InputStream currentContent;

    /**
     * Gets the current url.
     *
     * @return The current url the web view is at.
     */
    public String getCurrentUrl() {
        return currentUrl;
    }

    /**
     * Sets the current url the web view is at.
     *
     * @param currentUrl The current url.
     */
    public void setCurrentUrl(String currentUrl) {
        this.currentUrl = currentUrl;
        setChanged();
        notifyObservers(CURRENT_URL_PROP);
    }

    /**
     * Gets the content of the current url.
     *
     * @return The content of the current url.
     */
    public InputStream getCurrentContent() {
        return currentContent;
    }

    /**
     * Sets the content for the current url.
     *
     * @param currentContent The content for the current url.
     */
    public void setCurrentContent(InputStream currentContent) {
        this.currentContent = currentContent;
        setChanged();
        notifyObservers(CURRENT_CONTENT_PROP);
    }
}
