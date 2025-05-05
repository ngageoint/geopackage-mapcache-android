package mil.nga.mapcache.utils

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.concurrent.TimeUnit

class MatomoHeartBeatManager(private val heartbeatIntervalSeconds: Long = 15) : LifecycleEventObserver {

    companion object {
        private const val TAG = "MatomoHeartBeat"
    }

    private val scope = CoroutineScope(Dispatchers.IO)
    private var heartBeatJob: Job? = null

    private fun sendHeartbeat() {
        MatomoEventDispatcher.submitHeartBeat()
    }

    private fun startHeartBeatTimer() {
        heartBeatJob = scope.launch {
            while (true) {
                delay(TimeUnit.SECONDS.toMillis(heartbeatIntervalSeconds))
                sendHeartbeat()
            }
        }
    }

    private fun stopHeartbeatTimer() {
        heartBeatJob?.cancel()
        heartBeatJob = null
        Log.d(TAG, "HeartBeat timer stopped")
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_START -> {
                Log.d(TAG, "App moved to foreground. Starting matomo heart beat")
                startHeartBeatTimer()
            }
            Lifecycle.Event.ON_STOP -> {
                Log.d(TAG, "App moved to background. Stopping matomo heart beat")
                stopHeartbeatTimer()
            }
            else -> {}
        }
    }

    fun destroy() {
        stopHeartbeatTimer()
        scope.cancel()
    }
}