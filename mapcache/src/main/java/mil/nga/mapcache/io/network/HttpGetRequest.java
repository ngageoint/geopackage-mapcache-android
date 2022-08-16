package mil.nga.mapcache.io.network;

import android.app.Activity;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import mil.nga.mapcache.auth.Authenticator;
import mil.nga.mapcache.auth.UserLoggerInner;
import mil.nga.mapcache.utils.HttpUtils;

/**
 * An http get request class that performs any specified http get.
 */
public class HttpGetRequest implements Runnable, Authenticator {

    /**
     * Used to turn debug logging on.
     */
    private static final boolean isDebug = false;

    /**
     * The url of the get request.
     */
    private final String urlString;

    /**
     * Object that is called when request is completed.
     */
    private final IResponseHandler handler;

    /**
     * Used to get the app name and version for the user agent.
     */
    private final Activity activity;

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
    private Map<String, String> cookies = null;

    /**
     * Contains any previously saved cookies.
     */
    private final CookieJar allCookies;

    /**
     * Constructs a new HttpGetRequest.
     *
     * @param url      The url of the get request.
     * @param handler  Object this is called when request is completed.
     * @param activity Used to get the app name and version for the user agent.
     */
    public HttpGetRequest(String url, IResponseHandler handler, CookieJar allCookies, Activity activity) {
        this.urlString = url;
        this.handler = handler;
        this.activity = activity;
        this.allCookies = allCookies;
    }

    @Override
    public void run() {
        try {
            authorization = null;
            URL url = new URL(urlString);
            cookies = allCookies.getCookies(url.getHost());
            connect(url);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED
                    && (urlString.startsWith("https") || urlString.contains("10.0.2.2"))) {
                addBasicAuth(connection.getURL());
                responseCode = connection.getResponseCode();
            }

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                this.handler.handleResponse(null, responseCode);
            } else {
                InputStream stream = connection.getInputStream();
                String encoding = connection.getHeaderField(HttpUtils.getInstance().getContentEncodingKey());
                if (encoding != null && encoding.equals("gzip")) {
                    stream = new GZIPInputStream(stream);
                }
                this.handler.handleResponse(stream, responseCode);
                if (this.handler instanceof RequestHeaderConsumer) {
                    Map<String, List<String>> headers = new HashMap<>();
                    if (this.authorization != null) {
                        List<String> authorizations = new ArrayList<>();
                        authorizations.add(this.authorization);
                        headers.put(HttpUtils.getInstance().getBasicAuthKey(), authorizations);
                    }

                    if (this.cookies != null) {
                        List<String> cookieValues = new ArrayList<>(this.cookies.values());
                        headers.put(HttpUtils.getInstance().getCookieKey(), cookieValues);
                    }
                    ((RequestHeaderConsumer) this.handler).setRequestHeaders(headers);
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

    @Override
    public boolean authenticate(URL url, String userName, String password) {
        boolean authorized = false;

        try {
            if (connection != null) {
                connection.disconnect();
            }

            String usernamePass = userName + ":" + password;
            authorization = "Basic " + Base64.encodeToString(usernamePass.getBytes(), Base64.NO_WRAP);
            if (isDebug)
                Log.d(HttpGetRequest.class.getSimpleName(), "Authenticating to " + urlString);
            connect(url);
            int responseCode = connection.getResponseCode();
            authorized = responseCode != HttpURLConnection.HTTP_UNAUTHORIZED;
        } catch (IOException e) {
            Log.e(HttpGetRequest.class.getSimpleName(), e.getMessage(), e);
        }

        return authorized;
    }

    /**
     * Adds basic auth to the connection.
     *
     * @param url The connection to add basic auth to.
     */
    private void addBasicAuth(URL url) {
        if (loggerInner == null) {
            loggerInner = new UserLoggerInner(activity);
        }
        loggerInner.login(url, this);
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
        connection.addRequestProperty("Accept", "application/json, text/plain, */*");
        connection.addRequestProperty("Accept-Encoding", "gzip, deflate, br");
        connection.addRequestProperty("Accept-Language", "en-US");
        connection.addRequestProperty("Connection", "keep-alive");
        connection.addRequestProperty("Host", connection.getURL().getHost());
        connection.addRequestProperty("Origin", "null");
        connection.addRequestProperty("Sec-Fetch-Dest", "empty");
        connection.addRequestProperty("Sec-Fetch-Mode", "cors");
        connection.addRequestProperty("Sec-Fetch-Site", "cross-site");

        if (authorization != null) {
            connection.addRequestProperty(HttpUtils.getInstance().getBasicAuthKey(), authorization);
        } else if (cookies != null) {
            for (String cookie : cookies.values()) {
                connection.addRequestProperty(HttpUtils.getInstance().getCookieKey(), cookie);
            }
        }
    }

    /**
     * Connects to the specified url.
     *
     * @param url The url to connect to.
     */
    private void connect(URL url) {
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            configureRequest(connection);
            if (isDebug) {
                Log.d(HttpGetRequest.class.getSimpleName(), "Connecting to " + url);
                for (Map.Entry<String, List<String>> entry : connection.getRequestProperties().entrySet()) {
                    Log.d(HttpGetRequest.class.getSimpleName(), entry.getKey() + ": " + entry.getValue());
                }
            }
            connection.connect();

            int responseCode = connection.getResponseCode();

            if (isDebug) {
                Log.d(HttpGetRequest.class.getSimpleName(), "Response code " + responseCode + " " + url);
                for (Map.Entry<String, List<String>> entries : connection.getHeaderFields().entrySet()) {
                    Log.d(HttpGetRequest.class.getSimpleName(), entries.getKey() + ": " + entries.getValue());
                }
            }
            checkCookie();
            while (responseCode == HttpURLConnection.HTTP_MOVED_PERM
                    || responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                    || responseCode == HttpURLConnection.HTTP_SEE_OTHER) {
                authorization = null;
                String redirect = connection.getHeaderField(HttpUtils.getInstance().getLocationKey());
                if (!redirect.startsWith("http")) {
                    URL original = new URL(urlString);
                    String hostAndPort = original.getAuthority();
                    String protocol = original.getProtocol();
                    redirect = protocol + "://" + hostAndPort + redirect;
                    authorization = null;
                }
                connection.disconnect();
                url = new URL(redirect);

                connection = (HttpURLConnection) url.openConnection();
                connection.setInstanceFollowRedirects(false);
                configureRequest(connection);
                if (isDebug) {
                    Log.d(HttpGetRequest.class.getSimpleName(), "Redirecting to " + url);
                    for (Map.Entry<String, List<String>> entry : connection.getRequestProperties().entrySet()) {
                        Log.d(HttpGetRequest.class.getSimpleName(), entry.getKey() + ": " + entry.getValue());
                    }
                }
                connection.connect();
                responseCode = connection.getResponseCode();
                if (isDebug) {
                    Log.d(HttpGetRequest.class.getSimpleName(), "Response code " + responseCode + " " + url);
                    for (Map.Entry<String, List<String>> entries : connection.getHeaderFields().entrySet()) {
                        Log.d(HttpGetRequest.class.getSimpleName(), entries.getKey() + ": " + entries.getValue());
                    }
                }
                checkCookie();
            }
        } catch (IOException e) {
            Log.e(HttpGetRequest.class.getSimpleName(), e.getMessage(), e);
        }
    }

    /**
     * Checks to see if the response has a cookie.
     */
    private void checkCookie() throws MalformedURLException {
        List<String> cookies = connection.getHeaderFields().get(HttpUtils.getInstance().getSetCookieKey());
        if (cookies != null && !cookies.isEmpty()) {
            if (this.cookies == null) {
                this.cookies = new HashMap<>();
            }
            for (String cookie : cookies) {
                if (isDebug)
                    Log.d(HttpGetRequest.class.getSimpleName(), "Cookie found: " + cookie);
                String[] nameValue = cookie.split("=", 2);
                this.cookies.put(nameValue[0], cookie);
            }
            URL originalUrl = new URL(urlString);
            allCookies.storeCookies(originalUrl.getHost(), this.cookies);
        }
    }
}
