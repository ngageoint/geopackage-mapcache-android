package mil.nga.mapcache.io.network;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.webkit.WebView;

import java.net.HttpURLConnection;
import java.util.Observable;
import java.util.Observer;

import mil.nga.mapcache.utils.ThreadUtils;

/**
 * Performs a get request using a WebView to do so.
 */
public class WebViewRequest implements Observer {

    /**
     * Debug logging flag.
     */
    private static final boolean isDebug = false;

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
     * The dialog showing the web page.
     */
    private AlertDialog alert;

    /**
     * The model used to keep track of the requests state.
     */
    private final WebViewRequestModel model = new WebViewRequestModel();

    /**
     * True if the web view is being shown to the user currently.
     */
    private boolean isShown = false;

    /**
     * Indicates if the original url is a download imager url.
     */
    private boolean isImageUrl = false;

    /**
     * Used to get the html for the current url.
     */
    private final WebViewContentRetriever jsInterface;

    /**
     * True if this request has already received a response.
     */
    private boolean receivedResponse = false;

    /**
     * The number of times we have attempted reconnection.
     */
    private int retryCount = 0;

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
        this.model.addObserver(this);
        WebViewRequestClient client = new WebViewRequestClient(this.activity, this.model);
        this.webView.setWebViewClient(client);
        this.jsInterface = new WebViewContentRetriever(this.activity, this.webView, this.model);
    }

    /**
     * Executes the request.
     */
    public void execute() {
        if (urlString.contains("format=image")) {
            isImageUrl = true;
        }
        this.webView.layout(0, 0, 1000, 1000);
        if(isDebug) {
            Log.d(WebViewRequest.class.getSimpleName(), "Executing request " + this.urlString);
        }
        this.webView.loadUrl(this.urlString);
        ThreadUtils.getInstance().runBackground(this::checkIfDead);
    }

    /**
     * Checks to see if the WebView object crashed and we need to restart it.
     */
    private void checkIfDead() {
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            Log.d(WebViewRequest.class.getSimpleName(), e.getMessage(), e);
        }

        if(!receivedResponse) {
            if(retryCount < 3) {
                retryCount++;
                Log.i(
                        WebViewRequest.class.getSimpleName(),
                        "Connection went stale attempting to reconnect " + this.urlString);
                this.activity.runOnUiThread(() -> {
                    this.webView.stopLoading();
                    this.execute();
                });
            } else {
                Log.w(
                        WebViewRequest.class.getSimpleName(),
                        "Attempted to connect " + retryCount
                                + " times. Giving up on " + this.urlString);
            }
        }
    }

    /**
     * Shows the login web page in an alert dialog.
     */
    private void show() {
        if(isDebug) {
            Log.d(WebViewRequest.class.getSimpleName(), "Showing web page " + this.urlString);
        }
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this.activity);

        alertBuilder.setView(this.webView);
        alertBuilder.setNegativeButton("Close", (DialogInterface dialog, int id) ->
                dialog.dismiss()
        );
        alert = alertBuilder.create();
        alert.show();
        isShown = true;
    }

    @Override
    public void update(Observable observable, Object o) {
        if(isDebug) {
            Log.d(WebViewRequest.class.getSimpleName(), "Model updated " + o);
            Log.d(WebViewRequest.class.getSimpleName(), "Current url " + this.model.getCurrentUrl());
            Log.d(WebViewRequest.class.getSimpleName(), "Current content " + this.model.getCurrentContent());
        }
        if (WebViewRequestModel.CURRENT_URL_PROP.equals(o)) {
            if (!isShown && this.model.getCurrentUrl().contains("login.")) {
                // redirected to a login page so we need to show the WebView.
                show();
            } else if (isShown && !isImageUrl) {
                alert.dismiss();
                isShown = false;
            }
        } else if (WebViewRequestModel.CURRENT_CONTENT_PROP.equals(o)) {
            jsInterface.close();
            if (isShown) {
                alert.dismiss();
                isShown = false;
            }

            if(isDebug) {
                Log.d(
                        WebViewRequest.class.getSimpleName(),
                        "Send response to handler " + this.model.getCurrentUrl());
            }
            handler.handleResponse(this.model.getCurrentContent(), HttpURLConnection.HTTP_OK);
            this.receivedResponse = true;
        }
    }
}
