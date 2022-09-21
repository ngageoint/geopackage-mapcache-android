package mil.nga.mapcache.io.network.slowserver;

/**
 * Model containing the message to display to the user to notify them of a slow server.
 */
public class SlowServerModel {

    /**
     * The host of the slow server.
     */
    private String host;

    /**
     * The message to display to the user.
     */
    private String message;

    /**
     * Gets the host name of the slow server.
     *
     * @return The host name.
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the host name of the slow server.
     *
     * @param host The host name.
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Gets the message to display to the user.
     *
     * @return The message to display to the user.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message to display to the user.
     *
     * @param message The message to display to the user.
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
