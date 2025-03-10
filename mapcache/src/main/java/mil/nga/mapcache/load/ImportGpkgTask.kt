package mil.nga.mapcache.load

import android.app.Dialog
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import mil.nga.geopackage.io.GeoPackageProgress
import mil.nga.mapcache.R

sealed class ImportGpkgTask(private val frag: Fragment): GeoPackageProgress {

    private var max: Int = 0
    private var progress = 0
    protected var isCancelled = false

    private lateinit var importProgressBar: ProgressBar
    private lateinit var importPercentageText: TextView

    private val context = frag.requireContext()
    private val handler: Handler = Handler(Looper.getMainLooper())


    protected fun createProgressBarDialog(databaseName: String): Dialog {
        val inflater = LayoutInflater.from(context)
        val importDialogView = inflater.inflate(R.layout.import_progress_layout, null)
        val dialogBuilder = AlertDialog.Builder(context,
            R.style.AppCompatAlertDialogStyle
        )

        val bodyText = importDialogView.findViewById<TextView>(R.id.import_msg)
        bodyText.text = context.getString(R.string.import_body, databaseName)

        importPercentageText = importDialogView.findViewById(R.id.import_percentage)
        importProgressBar = importDialogView.findViewById(R.id.importProgressBar)

        dialogBuilder.setView(importDialogView)
        dialogBuilder.setCancelable(false)
        dialogBuilder.setNegativeButton(context.getString(R.string.button_cancel_label)) { dlg, btn ->
            dlg.dismiss()
            isCancelled = true
        }

        return dialogBuilder.create()
    }

    override fun setMax(max: Int) {
        this.max = max
    }

    override fun addProgress(progress: Int) {
        this.progress += progress
        val percentComplete = (this.progress / (max.toDouble()) * 100).toInt()

        //update progress bar values on main thread
        handler.post {
            importProgressBar.progress = percentComplete
            importPercentageText.text = context.getString(R.string.import_percentage, percentComplete)
        }
    }

    override fun isActive(): Boolean {
        return !isCancelled
    }

    override fun cleanupOnCancel(): Boolean {
        return true
    }

    protected fun showImportNameConflictDialog(name: String, action: () -> Unit) {
        AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle)
            .setTitle(R.string.import_name_conflict_title)
            .setMessage(context.getString(R.string.import_name_conflict_body, name))
            .setPositiveButton(context.getString(R.string.button_ok_label)) { dialog, id ->
                dialog.cancel()
                action()
            }.show()
    }

}