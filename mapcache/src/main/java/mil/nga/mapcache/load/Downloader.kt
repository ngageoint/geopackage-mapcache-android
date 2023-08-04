package mil.nga.mapcache.load

import android.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import mil.nga.geopackage.io.GeoPackageProgress
import mil.nga.mapcache.viewmodel.GeoPackageViewModel
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Downloads a GeoPackage via the GeoPackageViewModel
 */
class Downloader(val activity : FragmentActivity) : GeoPackageProgress{

    private var max: Int = 0
    private var progress: Int = 0
    private val alertDialog: AlertDialog
    private val myExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    init {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Import")
        builder.setPositiveButton("Cancel") {alertDialog, which ->
            myExecutor.shutdownNow()
        }
        alertDialog = builder.create()
    }


    fun downloadGeoPackage(viewModel : GeoPackageViewModel, url : String, database : String){
        val theUrl = URL(url)
        alertDialog.setMessage("Importing: $database")
        alertDialog.show()
        myExecutor.submit {
            try {
                if (!viewModel.importGeoPackage(database, theUrl, this)) {
                    val failure = "Failed to import GeoPackage '$database' at url '$url'"
                    alertDialog.dismiss()
                } else {
                    val success = "GeoPackage imported"
                    alertDialog.dismiss()
                }
            } catch (e: InterruptedException){
                Thread.currentThread().interrupt()
            }
        }
    }

    override fun setMax(max: Int) {
        this.max = max
    }

    override fun addProgress(progress: Int) {
        this.progress += progress
    }

    override fun isActive(): Boolean {
        return true
    }

    override fun cleanupOnCancel(): Boolean {
        return true
    }
}