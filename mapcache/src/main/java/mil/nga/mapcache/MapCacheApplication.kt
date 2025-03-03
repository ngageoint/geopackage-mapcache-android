package mil.nga.mapcache

import android.app.Application
import mil.nga.mapcache.io.CacheDirectoryType
import mil.nga.mapcache.io.MapCacheFileUtils

class MapCacheApplication: Application() {
    companion object {
        lateinit var appInstance: MapCacheApplication
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
        deleteCacheFiles()
    }

}

