package mil.nga.mapcache.io.network.slowserver;

/**
 * Model containing the message to display to the user to notify them of a slow server.
 */
public class SlowServerModel {

    /**
     * The message to display to the user.
     */
    private String message;

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
