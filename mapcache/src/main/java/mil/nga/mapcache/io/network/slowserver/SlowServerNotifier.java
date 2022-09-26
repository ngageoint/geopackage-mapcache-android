package mil.nga.mapcache.io.network.slowserver;

import android.app.Activity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Notifies the user of a slow server depending on its response times.
 */
public class SlowServerNotifier {

    /**
     * The time in milliseconds we consider a slow response.
     */
    private static final long slowResponseTime = 2000;

    /**
     * The number of slow responses until we consider the server as being slow.
     */
    private static final int slowResponseCount = 10;

    /**
     * The current slow response counts per server.
     */
    private final Map<String, Integer> slowResponseCounts = new HashMap<>();

    /**
     * The slow servers we have already notified the user about.
     */
    private final Set<String> notified = new HashSet<>();

    /**
     * The applications activity.
     */
    private final Activity activity;

    /**
     * Constructor.
     *
     * @param activity The applications activity.
     */
    public SlowServerNotifier(Activity activity) {
        this.activity = activity;
    }

    /**
     * Takes note of the host's response time.  If there are too many slow responses, this class will
     * notify the user that this server is slow.
     *
     * @param host         The host data was just downloaded from.
     * @param responseTime The response time in milliseconds of that download.
     */
    public void responseTime(String host, long responseTime) {
        if (responseTime >= slowResponseTime) {
            Integer slowCount = slowResponseCounts.get(host);
            if (slowCount == null) {
                slowCount = 0;
            }
            slowCount++;
            slowResponseCounts.put(host, slowCount);
            if (slowCount >= slowResponseCount && !notified.contains(host)) {
                SlowServerModel model = new SlowServerModel();
                model.setMessage("Downloads from " + host + " are taking a long time.  " +
                        "Either your connection is poor or the server's performance is slow.");
                SlowServerView view = new SlowServerView(model);
                view.show(this.activity);
                notified.add(host);
            }
        }
    }
}
