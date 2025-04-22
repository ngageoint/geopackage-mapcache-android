package mil.nga.mapcache.load

import android.app.Dialog
import android.content.DialogInterface
import android.text.Editable
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mil.nga.mapcache.R
import mil.nga.mapcache.utils.SampleDownloader
import mil.nga.mapcache.viewmodel.GeoPackageViewModel
import java.net.URL

class ImportGpkgDownloadTask(private val frag: Fragment, private val viewModel : GeoPackageViewModel): ImportGpkgTask(frag) {

    private val context = frag.requireContext()

    private lateinit var importProgressDialog: Dialog

    private lateinit var inputNameEditText: TextInputEditText
    private var inputNameValue = ""

    private lateinit var inputUrlEditText: TextInputEditText
    private var inputUrlValue = ""

    fun showDownloadDialog() {
        val inflater = LayoutInflater.from(context)
        val importUrlView: View = inflater.inflate(R.layout.import_url, null)
        val dialogBuilder = AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle)
        dialogBuilder.setView(importUrlView)

        val inputLayoutName = importUrlView.findViewById<TextInputLayout>(R.id.import_url_name_layout)
        val inputLayoutUrl = importUrlView.findViewById<TextInputLayout>(R.id.import_url_layout)

        inputNameEditText = importUrlView.findViewById(R.id.import_url_name_input)
        inputUrlEditText = importUrlView.findViewById(R.id.import_url_input)
        if (!TextUtils.isEmpty(inputNameValue)) {
            inputNameEditText.setText(inputNameValue)
        }
        if (!TextUtils.isEmpty(inputUrlValue)) {
            inputUrlEditText.setText(inputUrlValue)
        }

        //validate input when text is changed
        inputNameEditText.doAfterTextChanged {
            inputLayoutName.isErrorEnabled = false
            isInputValid(inputLayoutName, it, false)
        }

        inputUrlEditText.doAfterTextChanged {
            inputLayoutUrl.isErrorEnabled = false
            isInputValid(inputLayoutUrl, it, true)
        }

        //example GeoPackages link handler
        importUrlView.findViewById<View>(R.id.import_examples).setOnClickListener {
            showSampleGpkgs()
        }

        //wait to set the positive action until after the dialog is shown
        dialogBuilder.setPositiveButton(context.getString(R.string.geopackage_import_label)) { _, _ -> }

        dialogBuilder.setNegativeButton(context.getString(R.string.button_cancel_label)) { dlg: DialogInterface, id: Int ->
            dlg.cancel()
        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        //set click listener here in order to override the behavior of the dialog being automatically dismissed on positive button click
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener() {
            val nameValid: Boolean = isInputValid(inputLayoutName, inputNameEditText.text, false)
            val urlValid: Boolean = isInputValid(inputLayoutUrl, inputUrlEditText.text, true)

            //validate name and url input before attempting download
            if (nameValid && urlValid) {
                inputNameValue = inputNameEditText.text.toString()
                inputUrlValue = inputUrlEditText.text.toString()
                alertDialog.dismiss()

                initiateDownloadAfterGpkgNameValidation(inputNameValue, inputUrlValue)
            } else if (!nameValid) {
                inputNameEditText.requestFocus()
            } else {
                inputUrlEditText.requestFocus()
            }
        }
    }

    private fun initiateDownloadAfterGpkgNameValidation(name: String, url: String) {
        //check if a database already exists with the same name
        if (viewModel.geoPackageNameExists(name)) {
            showImportNameConflictDialog(name) {
                showDownloadDialog()
            }
        } else {
            //show progress dialog and launch download action
            importProgressDialog = createProgressBarDialog(name)
            importProgressDialog.show()

            viewModel.viewModelScope.launch(Dispatchers.IO) {
                downloadGpkgFromUrl(name, url)
            }
        }
    }

    //download the gpkg from url
    private suspend fun downloadGpkgFromUrl(name: String, url: String) {
        try {
            val gpkgUrl = URL(url)

            val isDownloadSuccess = viewModel.downloadGeoPackageViaUrl(name, gpkgUrl, this)

            withContext(Dispatchers.Main) {
                importProgressDialog.dismiss()

                if (!isDownloadSuccess && !isCancelled) {
                    showDownloadFailureDialog(name, url)
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                importProgressDialog.dismiss()
                showDownloadFailureDialog(name, url)
            }
        }
    }

    //download sample geopackages from our github server, and combine that list with our own locally provided preloaded geopackages
    private fun showSampleGpkgs() {
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(context, android.R.layout.select_dialog_item)

        val sampleDownloader = SampleDownloader(frag.requireActivity(), adapter)
        sampleDownloader.loadLocalGeoPackageSamples()
        sampleDownloader.getExampleData(context.getString(R.string.sample_geopackage_url))

        val builder = AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle)
        builder.setTitle(context.getString(R.string.import_url_preloaded_label))
        builder.setAdapter(adapter) { dlg: DialogInterface?, item: Int ->
            if (item >= 0) {
                val name = adapter.getItem(item)
                inputNameEditText.setText(name)
                inputUrlEditText.setText(sampleDownloader.sampleList[name])
            }
        }

        val alert = builder.create()
        alert.show()
    }

    private fun showDownloadFailureDialog(name: String, url: String) {
        AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle)
            .setTitle(R.string.import_title)
            .setMessage(context.getString(R.string.import_download_failure, name, url))
            .setPositiveButton(context.getString(R.string.button_ok_label)) { dialog, id ->
                dialog.cancel()
                showDownloadDialog()
            }.show()
    }

    private fun isInputValid(inputLayout: TextInputLayout, text: Editable?, isUrl: Boolean): Boolean {
        val errorMsg = context.getString(if (isUrl) R.string.err_msg_invalid_url else R.string.err_msg_invalid)

        if (text.isNullOrBlank()) {
            inputLayout.error = errorMsg
            return false
        } else if (isUrl && !Patterns.WEB_URL.matcher(text.toString()).matches()) {
            inputLayout.error = errorMsg
            return false
        } else {
            return true
        }
    }
}
