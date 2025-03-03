package mil.nga.mapcache.load

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import mil.nga.geopackage.GeoPackageConstants
import mil.nga.geopackage.io.GeoPackageIOUtils
import mil.nga.mapcache.GeoPackageMapFragment.GpkgDataToExport
import mil.nga.mapcache.GeoPackageUtils
import mil.nga.mapcache.MapCacheFileProvider
import mil.nga.mapcache.R
import mil.nga.mapcache.io.CacheDirectoryType
import mil.nga.mapcache.io.MapCacheFileUtils
import java.io.File
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Copy gpkg data to a cache file and share with external apps via an Intent
 */
class ShareGpkgExecutor(val activity : Activity, val gpkgData: GpkgDataToExport) {
    private val myExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private lateinit var actionLabel: TextView

    fun shareDatabaseViaIntent() {
        try {
            //data must first be written to cache before it can be shared via FileProvider
            shareGeoPackage(gpkgData.geoPackageFile, gpkgData.geoPackageName)

        } catch (e: Exception) {
            GeoPackageUtils.showMessage(activity, "Error sharing GeoPackage", e.message)
        }
    }

    //copy the gpkg data to a file in cache in order to share it via path supported by FileProvider
    private fun shareGeoPackage(gpkgFile : File, geoPackageName : String){
        //delete cached files prior to storing a new one
        MapCacheFileUtils.deleteCacheFiles(CacheDirectoryType.databases)

        val databaseCacheDir = MapCacheFileUtils.getCacheDirectory(CacheDirectoryType.databases)
        val cacheFile = File(databaseCacheDir, geoPackageName + "." + GeoPackageConstants.EXTENSION)

        //show dialog for status of writing the file
        val alertDialog = createShareStatusDialog()
        alertDialog.show()

        //create a handler to dismiss dialog on main thread
        val handler = android.os.Handler(Looper.getMainLooper())
        var failedShare = false

        myExecutor.submit {
            try {
                //write gpkg file to the cache file
                GeoPackageIOUtils.copyFile(gpkgFile, cacheFile)
            } catch (e: IOException) {
                actionLabel.text = activity.getString(R.string.share_exception, e)
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(R.string.button_ok_label)
                failedShare = true
            } catch (e: InterruptedException){
                actionLabel.text = activity.getString(R.string.share_interrupted)
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(R.string.button_ok_label)
                failedShare = true
                Thread.currentThread().interrupt()
            }
            handler.post {
                //if the file was successfully written, share it via an Intent
                if(!failedShare) {
                    alertDialog.dismiss()
                    launchShareIntent(cacheFile)
                }
            }
        }
    }

    //share the file via ACTION_SEND Intent
    private fun launchShareIntent(gpkgFile: File) {
        //obtain the content Uri for the file
        val gpkgFileContentUri = MapCacheFileProvider.getUriForFile(gpkgFile)

        //create Intent and add read permission
        val shareIntent = Intent()
        shareIntent.setAction(Intent.ACTION_SEND)
        shareIntent.setType("application/geopackage+sqlite3")
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        shareIntent.putExtra(Intent.EXTRA_STREAM, gpkgFileContentUri)

        activity.startActivity(Intent.createChooser(shareIntent, "Share"))
    }

    private fun createShareStatusDialog(): AlertDialog {
        val builder = AlertDialog.Builder(activity, R.style.AppCompatAlertDialogStyle)
        val inflater = LayoutInflater.from(activity)

        val alertView: View = inflater.inflate(R.layout.basic_label_alert, null)
        val alertLogo = alertView.findViewById<AppCompatImageView>(R.id.alert_logo)
        alertLogo.setImageResource(R.drawable.material_share)
        val titleText = alertView.findViewById<TextView>(R.id.alert_title)
        titleText.setText(R.string.geopackage_share_label)
        actionLabel = alertView.findViewById<View>(R.id.action_label) as TextView
        actionLabel.setText(R.string.geopackage_share_copy_message)
        actionLabel.visibility = View.VISIBLE

        builder.setPositiveButton("Cancel") {alertDialog, which ->
            myExecutor.shutdownNow()
        }

        builder.setView(alertView)
        builder.setCancelable(false)
        return builder.create()
    }
}