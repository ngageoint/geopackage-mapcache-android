package mil.nga.mapcache.load

import android.app.Activity
import android.app.AlertDialog
import android.os.Environment
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
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
 * Save a file to the downloads directory
 */
class SaveToDiskExecutor(val activity : Activity) {

    private val myExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var geoPackageName : String = ""
    private val alertDialog: AlertDialog

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
        val actionLabel = alertView.findViewById<View>(R.id.action_label) as TextView
        actionLabel.setText(R.string.geopackage_save_message)
        actionLabel.visibility = View.VISIBLE

        // Cancel button
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
        val downloadDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        cacheDir.mkdir()
        val handler = android.os.Handler(Looper.getMainLooper())
        var cacheFile = File(cacheDir, geoPackageName + "." + GeoPackageConstants.EXTENSION)

        // If file already exists, add a number on the end to ensure we don't overwrite
        var fileNumber = 0
        while (cacheFile.exists()) {
            fileNumber++
            cacheFile = File(
                downloadDir, geoPackageName + fileNumber + "."
                        + GeoPackageConstants.EXTENSION
            )
        }
        myExecutor.submit {
            try {
                GeoPackageIOUtils.copyFile(gpkgFile, cacheFile)
            } catch (e: IOException) {
                System.out.println("IOException: $e")
            } catch (e: InterruptedException){
                System.out.println("Exception: $e")

                Thread.currentThread().interrupt()
            }
            handler.post {
                deleteCachedDatabaseFiles(cacheDir)
                Toast.makeText(activity, "GeoPackage saved to Downloads", Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()
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