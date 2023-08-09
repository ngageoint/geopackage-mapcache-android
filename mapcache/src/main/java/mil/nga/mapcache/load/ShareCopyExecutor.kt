package mil.nga.mapcache.load

import android.app.AlertDialog
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
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
 * Copy an internal database to a shareable location and share
 */
class ShareCopyExecutor(val activity : FragmentActivity) {

    private val alertDialog: AlertDialog
    private val myExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var geoPackageName : String = ""

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
        val actionLabel = alertView.findViewById<View>(R.id.action_label) as TextView
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
        myExecutor.submit {
            try {
                GeoPackageIOUtils.copyFile(gpkgFile, cacheFile)
            } catch (e: IOException) {

            } catch (e: InterruptedException){
                Thread.currentThread().interrupt()
            }
            handler.post {
                alertDialog.dismiss()
            }
        }
    }
}