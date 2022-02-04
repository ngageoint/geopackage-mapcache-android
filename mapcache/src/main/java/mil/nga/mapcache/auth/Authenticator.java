package mil.nga.mapcache.auth;

import java.net.URL;

/**
 * Authenticates a username and password against a given url.
 */
public interface Authenticator {

    /**
     * Authenticates the username and password against the passed in url.
     *
     * @param url      The url to authenticate with.
     * @param userName The user's username.
     * @param password The user's password.
     * @return True if authentication is successful, false if it failed.
     */
    public boolean authenticate(URL url, String userName, String password);
}
