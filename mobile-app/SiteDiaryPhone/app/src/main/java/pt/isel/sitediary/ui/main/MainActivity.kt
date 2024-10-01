package pt.isel.sitediary.ui.main

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import pt.isel.sitediary.SiteDiaryApplication
import pt.isel.sitediary.domain.Loaded
import pt.isel.sitediary.domain.idle
import pt.isel.sitediary.ui.common.ErrorAlert
import pt.isel.sitediary.ui.login.LoginActivity
import pt.isel.sitediary.ui.viewmodel.MainViewModel
import pt.isel.sitediary.ui.work.WorkDetailsActivity
import java.io.File


class MainActivity : ComponentActivity() {

    private val app by lazy { application as SiteDiaryApplication }
    private val viewModel by viewModels<MainViewModel>(
        factoryProducer = {
            MainViewModel.factory(
                app.workService,
                app.logService,
                app.userService,
                app.loggedUserRepository
            )
        }
    )
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private val selectedFiles = hashMapOf<String, File>()
    private var flag = false

    companion object {
        fun navigateTo(origin: ComponentActivity) {
            val intent = Intent(origin, MainActivity::class.java)
            origin.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(ContentValues.TAG, "MainActivity.onCreate() called")
        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK && result.data != null) {
                    handleSelectedImages(result.data!!)
                }
            }
        viewModel.getAllValues()
        setContent {
            val mainValues by viewModel.mainValues.collectAsState(initial = idle())
            MainView(
                mainValues = mainValues,
                onRefresh = {
                    viewModel.refresh()
                },
                onWorkSelected = { workId ->
                    Log.v(ContentValues.TAG, "MainActivity.onWorkSelected() workId: $workId")
                    WorkDetailsActivity.navigateTo(this, workId)
                },
                onLogSelected = { logId ->
                    Log.v(ContentValues.TAG, "Log selected: $logId")
                    viewModel.getLog(logId)
                },
                onLogBackRequested = {
                    viewModel.clearSelectedLog()
                },
                onUploadRequested = {
                    flag = false
                    openGalleryMultiple()
                },
                onEditSubmit = {
                    viewModel.editLog(it)
                },
                onDeleteSubmit = { fileId, fileType ->
                    viewModel.deleteFile(fileId, fileType)
                },
                changeProfilePicture = {
                    flag = true
                    openGallery()
                },
                onLogoutRequest = {
                    viewModel.resetToIdle()
                    viewModel.logout()
                    LoginActivity.navigateTo(this)
                }
            )
            mainValues.let { values ->
                if (values is Loaded && values.value.isFailure) {
                    val msg = values.value.exceptionOrNull()?.message ?: "Something went wrong"
                    ErrorAlert(
                        title = "Ups!",
                        message = msg,
                        buttonText = "Ok",
                        onDismiss = {
                            viewModel.resetToIdle()
                            if (msg == "Login Required") {
                                viewModel.logout()
                                LoginActivity.navigateTo(this)
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.v(ContentValues.TAG, "MainActivity.onStart() called")
        viewModel.getAllValues()
    }

    override fun onResume() {
        super.onResume()
        Log.v(ContentValues.TAG, "WorkDetailsActivity.onResume() called")
        if (selectedFiles.isNotEmpty()) {
            if (flag) {
                Log.v(ContentValues.TAG, "EditProfilePicture called")
                viewModel.editProfilePicture(selectedFiles)
            } else {
                Log.v(ContentValues.TAG, "Update Log Files called")
                viewModel.uploadFiles(selectedFiles)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        Log.v(ContentValues.TAG, "MainActivity.onStop() called")
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
        }
        galleryLauncher.launch(intent)
    }

    private fun openGalleryMultiple() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        galleryLauncher.launch(intent)
    }

    private fun handleSelectedImages(data: Intent) {
        if (data.clipData != null) {
            val count = data.clipData!!.itemCount
            for (i in 0 until count) {
                val file = readFile(data.clipData!!.getItemAt(i).uri)
                if (file != null) selectedFiles[file.name] = file
            }
        } else if (data.data != null) {
            val file = readFile(data.data!!)
            if (file != null) selectedFiles[file.name] = file
        }
    }

    private fun readFile(uri: Uri): File? {
        val iS = contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("IMG_", ".jpg")
        return if (iS != null) {
            tempFile.outputStream().use { fileOutputStream ->
                iS.copyTo(fileOutputStream)
            }
            iS.close()
            tempFile
        } else null
    }
}

