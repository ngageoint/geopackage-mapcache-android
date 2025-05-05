package mil.nga.mapcache

import android.app.Application
import mil.nga.mapcache.io.CacheDirectoryType
import mil.nga.mapcache.io.MapCacheFileUtils
import org.matomo.sdk.Matomo
import org.matomo.sdk.Tracker
import org.matomo.sdk.TrackerBuilder

class MapCacheApplication: Application() {
    companion object {
        lateinit var appInstance: MapCacheApplication
            private set

        lateinit var matomoTracker: Tracker
            private set

        //delete files from cache directories
        fun deleteCacheFiles() {
            MapCacheFileUtils.deleteCacheFiles(CacheDirectoryType.images)
            MapCacheFileUtils.deleteCacheFiles(CacheDirectoryType.databases)
        }
    }

    override fun onCreate() {
        super.onCreate()
        appInstance = this
        matomoTracker = createMatomoTracker()

        deleteCacheFiles()
    }

    private fun createMatomoTracker(): Tracker {
        val siteUrl = getString(R.string.matomo_url)
        val siteId = resources.getInteger(R.integer.matomo_site_id)

        return TrackerBuilder(siteUrl, siteId, "MapCacheTracker").build(Matomo.getInstance(this))
    }

}

