package mil.nga.mapcache.io.network;

import java.util.List;
import java.util.Map;

/**
 * Interface to objects interested in the Authorization header value.
 */
public interface RequestHeaderConsumer {

    /**
     * Sets the request headers used during the most recent Http request.
     *
     * @param value The request headers, or null if one was not used.
     */
    void setRequestHeaders(Map<String, List<String>> value);
}
