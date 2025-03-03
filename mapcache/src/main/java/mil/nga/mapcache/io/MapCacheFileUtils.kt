package mil.nga.mapcache.io

import android.net.Uri
import android.provider.DocumentsContract
import android.text.TextUtils
import android.util.Log
import mil.nga.geopackage.io.GeoPackageIOUtils
import mil.nga.mapcache.MapCacheApplication.Companion.appInstance
import java.io.File

/**
 * MapCache File Utilities
 */
object MapCacheFileUtils {

    //get the display name from path or Uri
    fun getDisplayName(uri: Uri, path: String?): String {
        var name = ""

        //extract file name using path if available, otherwise use Uri
        if (!TextUtils.isEmpty(path)) {
            name = File(path).name
        } else {
            name = getDisplayNameFromUri(uri)
        }

        if (!TextUtils.isEmpty(name)) {
            //remove the .gpkg extension
            name = GeoPackageIOUtils.getFileNameWithoutExtension(name)
        } else {
            name = "default_gpkg"
        }

        return name
    }

    //Get display name from the Uri via ContentResolver
    private fun getDisplayNameFromUri(uri: Uri): String {
        var name = ""

        val resolver = appInstance.contentResolver
        val nameCursor = resolver.query(uri, null, null, null, null)
        try {
            if (nameCursor!!.count > 0) {
                val displayNameIndex = nameCursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                if (displayNameIndex >= 0 && nameCursor.moveToFirst()) {
                    name = nameCursor.getString(displayNameIndex)
                }
            }
        } catch (e: Exception) {
            Log.e("MapCacheFileUtils", "Error parsing name from uri")
        } finally {
            nameCursor?.close()
        }

        return name
    }

    //delete files from cache directory specified by CacheDirectoryType
    fun deleteCacheFiles(directory: CacheDirectoryType) {
        try {
            val cacheSubDir = getCacheDirectory(directory)
            if (cacheSubDir.exists()) {
                val cachedFiles = cacheSubDir.listFiles()
                if (cachedFiles != null) {
                    for (cacheFile in cachedFiles) {
                        cacheFile.delete()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MapCacheFileUtils", "Error deleting files from cache: " + e.message)
        }
    }

    //create specified cache directory if it doesn't already exist
    fun getCacheDirectory(directory: CacheDirectoryType): File {
        val cacheSubDir = File(appInstance.cacheDir, directory.name)
        cacheSubDir.mkdirs()
        return cacheSubDir
    }
}

enum class CacheDirectoryType {
    images, databases
}