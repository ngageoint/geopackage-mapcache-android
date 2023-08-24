package mil.nga.mapcache.load

import android.app.AlertDialog
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.FragmentActivity
import mil.nga.geopackage.io.GeoPackageProgress
import mil.nga.mapcache.R
import mil.nga.mapcache.viewmodel.GeoPackageViewModel
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Downloads a GeoPackage via the GeoPackageViewModel, providing feedback and cancel action via
 * an AlertDialog
 */
class Downloader(val activity : FragmentActivity) : GeoPackageProgress{

    private var max: Int = 0
    private var progress: Int = 0
    private val alertDialog: AlertDialog
    private val myExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var geoPackageName : String = ""

    /**
     * Create the alert dialog
     */
    init {
        val builder = AlertDialog.Builder(activity, R.style.AppCompatAlertDialogStyle)

        // Create Alert window with basic input text layout
        val inflater = LayoutInflater.from(activity)
        val alertView: View = inflater.inflate(R.layout.basic_label_alert, null)

        // Set dialog view info
        val alertLogo = alertView.findViewById<AppCompatImageView>(R.id.alert_logo)
        alertLogo.setImageResource(R.drawable.material_add_box)
        val titleText = alertView.findViewById<TextView>(R.id.alert_title)
        titleText.setText(R.string.import_geopackage_url)
        val actionLabel = alertView.findViewById<View>(R.id.action_label) as TextView
        actionLabel.setText("Importing GeoPackage")
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
     * Ask the viewmodel to download the given database from the given url.  Show the alert dialog
     * and allow cancel
     */
    fun downloadGeoPackage(viewModel : GeoPackageViewModel, url : String, database : String){
        val handler = android.os.Handler(Looper.getMainLooper())
        val theUrl = URL(url)
        var completeMessage : String = "Import failed"
        geoPackageName = database
        alertDialog.show()
        myExecutor.submit {
            try {
                if (!viewModel.importGeoPackage(database, theUrl, this)) {
                    completeMessage = "Failed to import GeoPackage '$database' at url '$url'"
                } else {
                    completeMessage = "GeoPackage imported"
                }
            } catch (e: InterruptedException){
                Thread.currentThread().interrupt()
            }
            handler.post {
                alertDialog.dismiss()
            }
        }
    }

    /**
     * Sets the max download
     */
    override fun setMax(max: Int) {
        this.max = max
    }

    /**
     * Add download progress, then update the alert dialog
     */
    override fun addProgress(progress: Int) {
        this.progress += progress
        val percentComplete = (this.progress / max.toDouble() * 100).toInt()
        val actionLabel = alertDialog.findViewById<View>(R.id.action_label) as TextView
        actionLabel.text = "Importing $geoPackageName: $percentComplete%"
    }

    override fun isActive(): Boolean {
        return true
    }

    override fun cleanupOnCancel(): Boolean {
        return true
    }
}