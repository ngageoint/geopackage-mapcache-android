package mil.nga.mapcache.io.network;

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
     * The current url that the web view is at.
     */
    private String currentUrl;

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
}
