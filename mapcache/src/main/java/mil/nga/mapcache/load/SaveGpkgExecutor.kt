package mil.nga.mapcache.load

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import mil.nga.geopackage.io.GeoPackageIOUtils
import mil.nga.mapcache.R
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Save a geopackage file to the user specified directory
 */
class SaveGpkgExecutor(val activity : Activity, val gpkgFile : File, val uri: Uri) {

    private val myExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private lateinit var actionLabel: TextView

    fun copyContentFromGpkgToNewFile() {
        val handler = android.os.Handler(Looper.getMainLooper())

        var statusMessage = "File saved to downloads"
        val alertDialog = createFeedbackDialog()
        alertDialog.show()

        myExecutor.submit {
            try {
                //get output stream to write to the file specified by uri
                val outputStream = activity.getContentResolver().openOutputStream(uri)

                //write gpkg contents to the output stream
                GeoPackageIOUtils.copyFile(gpkgFile, outputStream, null)
            } catch (e: InterruptedException){
                statusMessage = "File save interrupted"
                Thread.currentThread().interrupt()
            } catch (e: Exception) {
                statusMessage = "Error saving file: $e"
            }

            handler.post {
                actionLabel.text = statusMessage
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(R.string.button_ok_label)
            }
        }
    }

    private fun createFeedbackDialog(): AlertDialog {
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
        return builder.create()
    }
}