package mil.nga.mapcache.auth;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.webkit.HttpAuthHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.net.URL;

/**
 * Launches a WebView to allow the user to login to a specified url.
 */
public class WebViewLogin extends WebViewClient implements Authenticator {

    /**
     * The WebView used to log the user in.
     */
    private final WebView webView;

    /**
     * The activity this was launched from.
     */
    private final Activity activity;

    /**
     * The Url to log the user into.
     */
    private URL url;

    /**
     * The current auth handler.
     */
    private HttpAuthHandler currentHandler;

    /**
     * True if the user has been authenticated and we are done logging them in.
     */
    private boolean authenticated = false;

    /**
     * Constructor.
     *
     * @param activity The activity this was launched from.
     * @param webView  The WebView used to log the user in.
     */
    public WebViewLogin(Activity activity, WebView webView) {
        this.activity = activity;
        this.webView = webView;
        this.webView.setWebViewClient(this);
    }

    /**
     * Initiates the login for the user.
     *
     * @param url The url to login to.
     */
    public void login(URL url) {
        this.url = url;
        this.activity.runOnUiThread(this::show);

        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                Log.d(WebViewLogin.class.getSimpleName(), e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean authenticate(URL url, String userName, String password) {
        this.currentHandler.proceed(userName, password);
        authenticated = true;
        return true;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (authenticated) {
            synchronized (this) {
                this.notifyAll();
            }
        }
    }

    @Override
    public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
        this.currentHandler = handler;
        UserLoggerInner loggerInner = new UserLoggerInner(this.activity);
        loggerInner.login(this.url, this);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);

        return true;
    }

    /**
     * Shows the login web page in an alert dialog.
     */
    private void show() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this.activity);
        alert.setTitle("Title here");

        this.webView.setWebViewClient(this);
        this.webView.loadUrl(url.toString());

        alert.setView(this.webView);
        alert.setNegativeButton("Close", (DialogInterface dialog, int id) ->
                dialog.dismiss()
        );
        alert.show();
    }
}
