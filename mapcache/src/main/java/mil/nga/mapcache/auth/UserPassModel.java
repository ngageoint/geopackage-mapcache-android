package mil.nga.mapcache.auth;

import java.util.Observable;

/**
 * Model for the username and password dialog.
 */
public class UserPassModel extends Observable {

    /**
     * The username property.
     */
    public static String USERNAME_PROP = "usernameProp";

    /**
     * The password property.
     */
    public static String PASSWORD_PROP = "passwordProp";

    /**
     * The loginTo property.
     */
    public static String LOGIN_TO_PROP = "loginToProp";

    /**
     * The username.
     */
    private String username;

    /**
     * The password.
     */
    private String password;

    /**
     * The server requesting the login credentials.
     */
    private String loginTo;

    /**
     * Gets the username.
     * @return The username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     * @param username The new username.
     */
    public void setUsername(String username) {
        this.username = username;
        setChanged();
        notifyObservers(USERNAME_PROP);
    }

    /**
     * Gets the password.
     * @return The password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     * @param password The new password.
     */
    public void setPassword(String password) {
        this.password = password;
        setChanged();
        notifyObservers(PASSWORD_PROP);
    }

    /**
     * Gets the server requesting the login credentials.
     * @return The server requesting the login credentials.
     */
    public String getLoginTo() {
        return loginTo;
    }

    /**
     * Sets the server requesting the login credentials.
     * @param loginTo The server requesting the login credentials.
     */
    public void setLoginTo(String loginTo) {
        this.loginTo = loginTo;
        setChanged();
        notifyObservers(LOGIN_TO_PROP);
    }
}
