package mil.nga.mapcache.load

import android.app.Dialog
import android.content.DialogInterface
import android.net.Uri
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mil.nga.mapcache.GeoPackageUtils
import mil.nga.mapcache.R
import mil.nga.mapcache.io.MapCacheFileUtils.getDisplayName
import mil.nga.mapcache.viewmodel.GeoPackageViewModel

class ImportGpkgFileTask(private val frag: Fragment, private val viewModel: GeoPackageViewModel, private val uri: Uri): ImportGpkgTask(frag) {

    private val context = frag.requireContext()

    private lateinit var importProgressDialog: Dialog

    fun showImportFileDialog() {
        val inflater = LayoutInflater.from(frag.requireContext())
        val importFileView: View = inflater.inflate(R.layout.import_file, null)

        val dialogBuilder = AlertDialog.Builder(frag.requireContext(), R.style.AppCompatAlertDialogStyle)
        dialogBuilder.setView(importFileView)

        val nameInput: TextInputEditText = importFileView.findViewById(R.id.import_file_name_input)
        val inputLayoutName = importFileView.findViewById<TextInputLayout>(R.id.import_file_name_layout)

        val defaultName: String = getDisplayName(uri)
        nameInput.setText(defaultName)

        //validate input when text is changed
        nameInput.doAfterTextChanged {
            inputLayoutName.isErrorEnabled = false
            isInputValid(inputLayoutName, it)
        }

        //wait to set the positive action until after the dialog is shown
        dialogBuilder.setPositiveButton(frag.getString(R.string.import_title)) { _, _ -> }

        dialogBuilder.setNegativeButton(frag.getString(R.string.button_cancel_label)) { dialog, button ->
            dialog.dismiss()
        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        //set click listener here in order to override the behavior of the dialog being automatically dismissed on positive button click
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener() {
            val isNamePopulated = isInputValid(inputLayoutName, nameInput.text)

            if (isNamePopulated) {
                //import the gpkg by copying the file from content resolver uri
                val name = nameInput.text.toString()
                initiateCopyAfterGpkgNameValidation(name, uri)
                alertDialog.dismiss()
            } else {
                nameInput.requestFocus()
            }
        }
    }

    private fun initiateCopyAfterGpkgNameValidation(name: String, uri: Uri) {
        //check if a database already exists with the same name
        if (viewModel.geoPackageNameExists(name)) {
            showImportNameConflictDialog(name) {
                showImportFileDialog()
            }
        } else {
            //show progress dialog and launch file copy action
            importProgressDialog = createProgressBarDialog(name)
            importProgressDialog.show()

            viewModel.viewModelScope.launch(Dispatchers.IO) {
                importGpkgFile(name, uri)
            }
        }
    }

    //copy gpkg data using content resolver Uri
    private suspend fun importGpkgFile(name: String, uri: Uri) {
        try {
            val resolver = frag.requireContext().contentResolver
            val inputStream = resolver.openInputStream(uri)

            val isImportSuccess = viewModel.importGeoPackageViaFile(name, inputStream, this)

            withContext(Dispatchers.Main) {
                importProgressDialog.dismiss()

                if (!isImportSuccess && !isCancelled) {
                    GeoPackageUtils.showMessage(frag.requireActivity(), frag.getString(R.string.import_title),
                        frag.getString(R.string.import_file_failure, name))
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                importProgressDialog.dismiss()

                GeoPackageUtils.showMessage(frag.requireActivity(), frag.getString(R.string.import_title),
                    frag.getString(R.string.import_file_failure, name)
                )
            }
        }
    }

    private fun isInputValid(inputLayout: TextInputLayout, text: Editable?): Boolean {
        if (text.isNullOrBlank()) {
            inputLayout.error = context.getString(R.string.err_msg_invalid)
            return false
        } else {
            return true
        }
    }
}