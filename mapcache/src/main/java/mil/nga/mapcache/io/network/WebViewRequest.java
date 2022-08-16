package mil.nga.mapcache.io.network;

import android.app.Activity;
import android.webkit.WebView;

import java.net.MalformedURLException;
import java.net.URL;

import mil.nga.mapcache.auth.WebViewLogin;

/**
 * Performs a get request using a WebView to do so.
 */
public class WebViewRequest {

    /**
     * The get request url.
     */
    private final String urlString;

    /**
     * The object to notify of the response values.
     */
    private final IResponseHandler handler;

    /**
     * The activity that initiated the request.
     */
    private final Activity activity;

    /**
     * The WebView used to make the request.
     */
    private final WebView webView;

    /**
     * Constructor.
     *
     * @param url      The request url.
     * @param handler  The object to be notified of the results.
     * @param activity The activity that initiated the request.
     */
    public WebViewRequest(String url, IResponseHandler handler, Activity activity) {
        this.urlString = url;
        this.handler = handler;
        this.activity = activity;
        this.webView = new WebView(activity);
    }

    /**
     * Executes the request.
     *
     * @throws MalformedURLException If the request url is bad.
     */
    public void execute() throws MalformedURLException {
        WebViewLogin login = new WebViewLogin(this.activity, this.webView);
        login.login(new URL(this.urlString));
        handler.handleResponse(null, 200);
    }
}
