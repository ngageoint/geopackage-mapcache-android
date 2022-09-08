package mil.nga.mapcache.io.network;

import android.app.Activity;

import java.util.Map;

/**
 * Stores cookies for specific servers.
 */
public interface SessionManager {

    /**
     * Stores the cookies for the specified host.
     *
     * @param host    The host to store cookies for.
     * @param cookies The cookies to store.
     */
    void storeCookies(String host, Map<String, String> cookies);

    /**
     * Gets the cookies for the specified host.
     *
     * @param host The host to get the cookies for.
     * @return The cookies for the host, or null if there aren't any.
     */
    Map<String, String> getCookies(String host);

    /**
     * Executes the request at the specified url using a WebView.
     *
     * @param url      The request url.
     * @param handler  The response handler.
     * @param activity The activity that initiated the request.
     */
    void requestRequiresWebView(String url, IResponseHandler handler, Activity activity);
}
