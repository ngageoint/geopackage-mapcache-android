package mil.nga.mapcache.load

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.FileProvider
import mil.nga.geopackage.GeoPackageConstants
import mil.nga.geopackage.io.GeoPackageIOUtils
import mil.nga.mapcache.BuildConfig
import mil.nga.mapcache.R
import java.io.File
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Copy an internal database to a shareable location and share.  feedback provided by alertdialog
 */
class ShareCopyExecutor(val activity : Activity, val shareIntent : Intent) {

    private val alertDialog: AlertDialog
    private val myExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var geoPackageName : String = ""
    private val AUTHORITY = BuildConfig.APPLICATION_ID + ".fileprovider"
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
        titleText.setText(R.string.geopackage_share_label)
        actionLabel = alertView.findViewById<View>(R.id.action_label) as TextView
        actionLabel.setText(R.string.geopackage_share_copy_message)
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
     *  Copy the gpkgFile to the cacheDir with the geoPackageName
     */
    fun shareGeoPackage(cacheDir : File, gpkgFile : File, geoPackageName : String){
        this.geoPackageName = geoPackageName
        cacheDir.mkdir()
        val handler = android.os.Handler(Looper.getMainLooper())
        val cacheFile = File(cacheDir, geoPackageName + "." + GeoPackageConstants.EXTENSION)
        var failedShare: Boolean = false
        alertDialog.show()
        myExecutor.submit {
            try {
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
                if(!failedShare) {
                    alertDialog.dismiss()
                    // Create the content Uri and add intent permissions
                    val databaseUri = FileProvider.getUriForFile(activity, AUTHORITY, cacheFile)
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    launchShareIntent(shareIntent, databaseUri)
                }
            }
        }
    }

    /**
     * Share the file via system sharing
     */
    private fun launchShareIntent(shareIntent: Intent, databaseUri: Uri) {
        shareIntent.putExtra(Intent.EXTRA_STREAM, databaseUri)
        activity.startActivityForResult(
            Intent.createChooser(shareIntent, "Share"),
            ShareTask.ACTIVITY_SHARE_FILE
        )
    }
}