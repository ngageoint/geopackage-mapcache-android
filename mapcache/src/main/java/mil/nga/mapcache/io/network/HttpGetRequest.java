package mil.nga.mapcache.io.network;

import android.app.Activity;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import mil.nga.mapcache.auth.Authenticator;
import mil.nga.mapcache.auth.UserLoggerInner;
import mil.nga.mapcache.utils.HttpUtils;

/**
 * An http get request class that performs any specified http get.
 */
public class HttpGetRequest implements Runnable, Authenticator {

    /**
     * The url of the get request.
     */
    private String urlString;

    /**
     * Object that is called when request is completed.
     */
    private IResponseHandler handler;

    /**
     * Used to get the app name and version for the user agent.
     */
    private Activity activity;

    /**
     * Retrieves the user's username and password then calls back for authentication.
     */
    private UserLoggerInner loggerInner = null;

    /**
     * The http connection.
     */
    private HttpURLConnection connection = null;

    /**
     * The authorization string from previous Http request.
     */
    private String authorization = null;

    /**
     * A cookie to set in request if need be.
     */
    private String cookie = null;

    /**
     * Constructs a new HttpGetRequest.
     *
     * @param url      The url of the get request.
     * @param handler  Object this is called when request is completed.
     * @param activity Used to get the app name and version for the user agent.
     */
    public HttpGetRequest(String url, IResponseHandler handler, Activity activity) {
        this.urlString = url;
        this.handler = handler;
        this.activity = activity;
    }

    @Override
    public void run() {
        try {
            authorization = null;
            cookie = null;
            connect();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                addBasicAuth(connection);
                responseCode = connection.getResponseCode();
            }

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                this.handler.handleResponse(null, responseCode);
            } else {
                InputStream stream = connection.getInputStream();
                this.handler.handleResponse(stream, responseCode);
                if (this.handler instanceof AuthorizationConsumer) {
                    ((AuthorizationConsumer) this.handler).setAuthorizationValue(this.authorization);
                }
            }
        } catch (IOException e) {
            this.handler.handleException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Adds basic auth to the connection.
     *
     * @param connection The connection to add basic auth to.
     */
    private void addBasicAuth(HttpURLConnection connection) {
        if (loggerInner == null) {
            loggerInner = new UserLoggerInner(activity);
        }
        loggerInner.login(connection.getURL(), this);
    }

    /**
     * Adds the user agent to the http header.
     *
     * @param connection The connection to add the user agent to.
     */
    private void configureRequest(HttpURLConnection connection) {
        connection.addRequestProperty(
                HttpUtils.getInstance().getUserAgentKey(),
                HttpUtils.getInstance().getUserAgentValue(activity));

        // Used for debugging connection issues
        connection.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        connection.addRequestProperty("Accept-Language", "en-US,en;q=0.9");
        connection.addRequestProperty("Cache-Control", "no-cache");
        connection.addRequestProperty("Connection", "keep-alive");
        connection.addRequestProperty("Host", connection.getURL().getAuthority());
        connection.addRequestProperty("Sec-Fetch-Dest", "document");
        connection.addRequestProperty("Sec-Fetch-Mode", "navigate");
        connection.addRequestProperty("Sec-Fetch-Site", "none");
        connection.addRequestProperty("Sec-Fetch-User", "?1");
        connection.addRequestProperty("Upgrade-Insecure-Requests", "1");

        if(authorization != null) {
            connection.addRequestProperty(HttpUtils.getInstance().getBasicAuthKey(), authorization);
        }

        if(cookie != null) {
            connection.addRequestProperty(HttpUtils.getInstance().getCookieKey(), cookie);
        }
    }

    private void connect() {
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            configureRequest(connection);
            Log.i(HttpGetRequest.class.getSimpleName(), " ");
            Log.i(HttpGetRequest.class.getSimpleName(), " ");
            Log.i(HttpGetRequest.class.getSimpleName(), " ");
            Log.i(HttpGetRequest.class.getSimpleName(), "Connecting to " + url);
            for(Map.Entry<String, List<String>> entry : connection.getRequestProperties().entrySet()) {
                Log.i(HttpGetRequest.class.getSimpleName(), entry.getKey() + ": " + entry.getValue());
            }
            connection.connect();

            int responseCode = connection.getResponseCode();

            Log.i(HttpGetRequest.class.getSimpleName(), "Response code " + responseCode + " " + url);
            for (Map.Entry<String, List<String>> entries : connection.getHeaderFields().entrySet()) {
                Log.i(HttpGetRequest.class.getSimpleName(), entries.getKey() + ": " + entries.getValue());
            }
            checkCookie();
            while (responseCode == HttpURLConnection.HTTP_MOVED_PERM
                    || responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                    || responseCode == HttpURLConnection.HTTP_SEE_OTHER) {
                String redirect = connection.getHeaderField(HttpUtils.getInstance().getLocationKey());
                connection.disconnect();
                url = new URL(redirect);
                connection = (HttpURLConnection) url.openConnection();
                connection.setInstanceFollowRedirects(false);
                configureRequest(connection);
                Log.i(HttpGetRequest.class.getSimpleName(), "Redirecting to " + url);
                for(Map.Entry<String, List<String>> entry : connection.getRequestProperties().entrySet()) {
                    Log.i(HttpGetRequest.class.getSimpleName(), entry.getKey() + ": " + entry.getValue());
                }
                connection.connect();
                responseCode = connection.getResponseCode();
                checkCookie();
                Log.i(HttpGetRequest.class.getSimpleName(), "Response code " + responseCode + " " + url);
                for (Map.Entry<String, List<String>> entries : connection.getHeaderFields().entrySet()) {
                    Log.i(HttpGetRequest.class.getSimpleName(), entries.getKey() + ": " + entries.getValue());
                }
            }
        } catch (IOException e) {
            Log.e(HttpGetRequest.class.getSimpleName(), e.getMessage(), e);
        }
    }

    /**
     * Checks to see if the response has a cookie.
     */
    private void checkCookie() {
        String setCookie = connection.getHeaderField(HttpUtils.getInstance().getSetCookieKey());
        if(setCookie != null && !setCookie.isEmpty()) {
            cookie = setCookie;
            Log.i(HttpGetRequest.class.getSimpleName(), "Cookie found: " + cookie);
        }
    }

    @Override
    public boolean authenticate(URL url, String userName, String password) {
        boolean authorized = false;

        try {
            if (connection != null) {
                connection.disconnect();
            }

            String usernamePass = userName + ":" + password;
            authorization = "Basic " + Base64.encodeToString(usernamePass.getBytes(), Base64.NO_WRAP);
            Log.i(HttpGetRequest.class.getSimpleName(), "Authenticating to " + urlString);
            connect();
            int responseCode = connection.getResponseCode();
            authorized = responseCode != HttpURLConnection.HTTP_UNAUTHORIZED;
        } catch (IOException e) {
            Log.e(HttpGetRequest.class.getSimpleName(), e.getMessage(), e);
        }

        return authorized;
    }
}
