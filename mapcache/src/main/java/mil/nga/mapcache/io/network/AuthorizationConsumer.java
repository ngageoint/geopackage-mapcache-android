package mil.nga.mapcache.io.network;

/**
 * Interface to objects interested in the Authorization header value.
 */
public interface AuthorizationConsumer {

    /**
     * Sets the authorization value used during the most recent Http request.
     *
     * @param value The authorization value, or null if one was not used.
     */
    void setAuthorizationValue(String value);
}
