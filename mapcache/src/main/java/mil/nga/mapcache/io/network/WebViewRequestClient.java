package mil.nga.mapcache.io.network;

import android.app.Activity;
import android.util.Log;
import android.webkit.HttpAuthHandler;
import android.webkit.WebView;

import java.net.MalformedURLException;
import java.net.URL;

import mil.nga.mapcache.auth.Authenticator;
import mil.nga.mapcache.auth.UserLoggerInner;
import mil.nga.mapcache.utils.ThreadUtils;

/**
 * Launches a WebView to allow the user to login to a specified url.
 */
public class WebViewRequestClient extends android.webkit.WebViewClient implements Authenticator {

    /**
     * Debug logging flag.
     */
    private static final boolean isDebug = false;

    /**
     * The activity this was launched from.
     */
    private final Activity activity;

    /**
     * The current auth handler.
     */
    private HttpAuthHandler currentHandler;

    /**
     * The web view request model.
     */
    private final WebViewRequestModel model;

    /**
     * The request url.
     */
    private URL url = null;

    /**
     * Constructor.
     *
     * @param activity The activity this was launched from.
     * @param model    The web view request model.
     */
    public WebViewRequestClient(Activity activity, WebViewRequestModel model) {
        this.activity = activity;
        this.model = model;
    }

    @Override
    public boolean authenticate(URL url, String userName, String password) {
        if(isDebug) {
            Log.d(WebViewRequestClient.class.getSimpleName(), "Authenticate " + url);
        }
        this.currentHandler.proceed(userName, password);
        return true;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if(isDebug) {
            Log.d(WebViewRequestClient.class.getSimpleName(), "On Page Finished " + url);
        }

        if (this.url == null) {
            try {
                this.url = new URL(url);
            } catch (MalformedURLException e) {
                Log.e(WebViewRequestClient.class.getSimpleName(), e.getMessage(), e);
            }
        }

        this.activity.runOnUiThread(() -> this.model.setCurrentUrl(url));
    }

    @Override
    public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
        this.currentHandler = handler;
        if(isDebug) {
            Log.d(WebViewRequestClient.class.getSimpleName(), "On receive http auth request " + url);
        }
        ThreadUtils.getInstance().runBackground(() -> {
            UserLoggerInner loggerInner = new UserLoggerInner(this.activity);
            loggerInner.login(this.url, this);
        });
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if(isDebug) {
            Log.d(WebViewRequestClient.class.getSimpleName(), "should override url loading " + url);
        }
        view.loadUrl(url);

        return true;
    }
}
