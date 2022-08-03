package mil.nga.mapcache.io.network;

import java.util.Map;

/**
 * Stores cookies for specific servers.
 */
public interface CookieJar {

    /**
     * Stores the cookies for the specified host.
     *
     * @param host    The host to store cookies for.
     * @param cookies The cookies to store.
     */
    public void storeCookies(String host, Map<String, String> cookies);

    /**
     * Gets the cookies for the specified host.
     *
     * @param host The host to get the cookies for.
     * @return The cookies for the host, or null if there aren't any.
     */
    public Map<String, String> getCookies(String host);
}
