package mil.nga.mapcache.io.network;

import android.app.Activity;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import mil.nga.mapcache.io.network.slowserver.SlowServerNotifier;
import mil.nga.mapcache.utils.ThreadUtils;

/**
 * Makes http requests asynchronously.
 */
public class HttpClient implements SessionManager {

    /**
     * The instance of this class.
     */
    private static final HttpClient instance = new HttpClient();

    /**
     * Debug logging flag.
     */
    private static final boolean isDebug = false;

    /**
     * Any cookies being stored for http requests.
     */
    private final Map<String, Map<String, String>> allCookies = new HashMap<>();

    /**
     * The hosts that require a web view in order to download from given urls.
     */
    private final Set<String> webViewHosts = new HashSet<>();

    /**
     * If the server is slow, this will notify the user of that.
     */
    private SlowServerNotifier notifier = null;

    /**
     * Gets the instance of this class.
     *
     * @return This class instance.
     */
    public static HttpClient getInstance() {
        return instance;
    }

    /**
     * Sends a http get to the specified url.
     *
     * @param url     The url to send a get request to.
     * @param handler The response handler, called when request is complete.
     * @param activity Used to get the app name and version for the user agent.
     */
    public synchronized void sendGet(String url, IResponseHandler handler, Activity activity) {
        try {
            URL theUrl = new URL(url);
            String host = theUrl.getHost();
            boolean requiresWebView = webViewHosts.contains(host);

            if(notifier == null) {
                notifier = new SlowServerNotifier(activity);
            }

            ResponseMonitor monitor = new ResponseMonitor(host, handler, notifier);
            if(requiresWebView) {
                requestRequiresWebView(url, monitor, activity);
            } else {
                HttpGetRequest request = new HttpGetRequest(url, monitor, this, activity);
                ThreadUtils.getInstance().runBackground(request);
            }
        } catch (MalformedURLException e) {
            Log.e(HttpClient.class.getSimpleName(), e.getMessage(), e);
        }
    }

    /**
     * Private constructor, keep it a singleton.
     */
    private HttpClient() {
    }

    @Override
    public synchronized void storeCookies(String host, Map<String, String> cookies) {
        allCookies.put(host, cookies);
    }

    @Override
    public synchronized Map<String, String> getCookies(String host) {
        return allCookies.get(host);
    }

    @Override
    public synchronized void requestRequiresWebView(String url, IResponseHandler handler, Activity activity) {
        try {
            URL theUrl = new URL(url);
            webViewHosts.add(theUrl.getHost());
        } catch (MalformedURLException e) {
            Log.e(HttpClient.class.getSimpleName(), e.getMessage(), e);
        }
        if(isDebug) {
            Log.d(HttpClient.class.getSimpleName(), "Using web view for request " + url);
        }
        activity.runOnUiThread(()->{
            WebViewRequest request = new WebViewRequest(url, handler, activity);
            request.execute();
        });
    }
}
