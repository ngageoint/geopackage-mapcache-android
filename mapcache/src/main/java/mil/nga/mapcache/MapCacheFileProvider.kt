package mil.nga.mapcache

import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 *It is possible to declare a FileProvider directly in the manifest instead of extending it.
 *However, this is not reliable on all devices as some OEMs may strip the "paths" metadata from the manifest.
 *This implementation allows the paths data to be specified in code
 */

class MapCacheFileProvider : FileProvider(R.xml.share_paths) {

    companion object {
        val AUTHORITY = BuildConfig.APPLICATION_ID + ".fileprovider"

        //convenience method for FileProvider.getUriForFile
        fun getUriForFile(file: File): Uri {
            return getUriForFile(MapCacheApplication.appInstance, AUTHORITY, file)
        }
    }
}