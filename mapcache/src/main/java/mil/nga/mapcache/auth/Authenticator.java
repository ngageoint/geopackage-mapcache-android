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
    boolean authenticate(URL url, String userName, String password);

    /**
     * Indicates if we should save the account to the phone after a successful authentication.
     *
     * @return True if the account should be saved after successful authentication, false if it should not.
     */
    boolean shouldSaveAccount();
}
