package mil.nga.mapcache.load

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Build
import android.os.Environment
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.FragmentActivity
import mil.nga.geopackage.GeoPackageConstants
import mil.nga.geopackage.io.GeoPackageIOUtils
import mil.nga.mapcache.R
import java.io.File
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Save a file to the downloads directory using Executor threads and showing an alert dialog
 */
class SaveToDiskExecutor(val activity : Activity) {

    private val myExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val alertDialog: AlertDialog
    private val actionLabel: TextView

    /**
     * Create an alert dialog for feedback
     */
    init {
        val builder = AlertDialog.Builder(activity, R.style.AppCompatAlertDialogStyle)

        // Create Alert window with basic input text layout
        val inflater = LayoutInflater.from(activity)
        val alertView: View = inflater.inflate(R.layout.basic_label_alert, null)

        // Set dialog view info
        val alertLogo = alertView.findViewById<AppCompatImageView>(R.id.alert_logo)
        alertLogo.setImageResource(R.drawable.material_share)
        val titleText = alertView.findViewById<TextView>(R.id.alert_title)
        titleText.setText(R.string.geopackage_save_label)
        actionLabel = alertView.findViewById<View>(R.id.action_label) as TextView
        actionLabel.setText(R.string.geopackage_save_message)
        actionLabel.visibility = View.VISIBLE

        // Cancel button - interrupt thread
        builder.setPositiveButton("Cancel") {alertDialog, which ->
            myExecutor.shutdownNow()
        }

        builder.setView(alertView)
        builder.setCancelable(false)
        alertDialog = builder.create()
    }

    /**
     * Save the gpkgFile to the downloads directory using the cacheDir and geoPackageName
     */
    fun saveToDisk(cacheDir : File, gpkgFile : File, geoPackageName : String){
        // This appears to have been undeprecated in 2022
        val downloadDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        cacheDir.mkdir()
        val handler = android.os.Handler(Looper.getMainLooper())
        var cacheFile = File(downloadDir, geoPackageName + "." + GeoPackageConstants.EXTENSION)

        // If file already exists, add a number on the end to ensure we don't overwrite
        var fileNumber = 0
        while (cacheFile.exists()) {
            fileNumber++
            cacheFile = File(
                downloadDir, geoPackageName + fileNumber + "."
                        + GeoPackageConstants.EXTENSION
            )
        }
        var statusMessage: String = "File saved to downloads"
        alertDialog.show()
        myExecutor.submit {
            try {
                GeoPackageIOUtils.copyFile(gpkgFile, cacheFile)
            } catch (e: IOException) {
                statusMessage = "Error saving file: $e"
            } catch (e: InterruptedException){
                statusMessage = "File save interrupted"
                Thread.currentThread().interrupt()
            }
            handler.post {
                deleteCachedDatabaseFiles(cacheDir)
                actionLabel.text = statusMessage
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(R.string.button_ok_label)
            }
        }
    }


    /**
     * Delete the given temporary cache directory
     */
    private fun deleteCachedDatabaseFiles(cacheDir : File){
        if (cacheDir.exists()) {
            val cacheFiles: Array<File> = cacheDir.listFiles()
            for (cacheFile in cacheFiles) {
                cacheFile.delete()
            }
            cacheDir.delete()
        }
    }
}