package mil.nga.mapcache.io.network;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;

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
    private final Context context;

    /**
     * Constructor.
     *
     * @param context The applications activity.
     */
    public SlowServerNotifier(Context context) {
        this.context = context;
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
                String message = ("Downloads from " + host + " are taking a long time.  " +
                        "Either your connection is poor or the server's performance is slow.");
                showDialog(message);
                notified.add(host);
            }
        }
    }

    /**
     * Shows the dialog informing the user of a slow server.
     *
     * @param message The message to display to the user to notify them of a slow server.
     */
    private void showDialog(String message) {
        new Handler(Looper.getMainLooper()).post(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Slow Downloads");
            builder.setMessage(message);
            builder.setPositiveButton("Ok", (DialogInterface dialog, int arg1) -> dialog.dismiss());
            builder.show();
        });
    }
}
