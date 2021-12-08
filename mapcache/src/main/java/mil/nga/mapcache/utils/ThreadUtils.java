package mil.nga.mapcache.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility class that runs tasks on a background thread.
 */
public class ThreadUtils {

    /**
     * Instance of this class.
     */
    private static ThreadUtils instance = new ThreadUtils();

    /**
     * The thread pool.
     */
    private ExecutorService executor = Executors.newFixedThreadPool(16);

    /**
     * Gets the instance of this class.
     *
     * @return This class' instance.
     */
    public static ThreadUtils getInstance() {
        return instance;
    }

    /**
     * Runs the specified task on a background thread.
     *
     * @param task The task to run in the background.
     */
    public void runBackground(Runnable task) {
        executor.execute(task);
    }

    /**
     * Private constructor makes this class singleton.
     */
    private ThreadUtils() {

    }
}
