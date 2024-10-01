package pt.isel.sitediary.ui.work

import android.content.ContentValues
import android.content.Context
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import pt.isel.sitediary.SiteDiaryApplication
import pt.isel.sitediary.domain.Loaded
import pt.isel.sitediary.domain.LogInputModel
import pt.isel.sitediary.domain.idle
import pt.isel.sitediary.ui.common.ErrorAlert
import pt.isel.sitediary.ui.login.LoginActivity
import pt.isel.sitediary.ui.viewmodel.WorkViewModel
import java.io.File
import java.util.UUID

class WorkDetailsActivity : ComponentActivity() {

    private val app by lazy { application as SiteDiaryApplication }
    private val viewModel by viewModels<WorkViewModel>(
        factoryProducer = {
            WorkViewModel.factory(
                app.workService,
                app.logService,
                app.userService,
                app.loggedUserRepository
            )
        }
    )
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private val selectedFiles = hashMapOf<String, File>()
    var content = ""
    private var flag = false

    companion object {
        fun navigateTo(ctx: Context, workId: UUID?) {
            if (workId == null) {
                return
            }
            ctx.startActivity(createIntent(ctx, workId))
        }

        private fun createIntent(ctx: Context, workId: UUID): Intent {
            val intent = Intent(ctx, WorkDetailsActivity::class.java)
            intent.putExtra("workId", workId.toString())
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(ContentValues.TAG, "WorkDetailsActivity.onCreate() called")
        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK && result.data != null) {
                    handleSelectedImages(result.data!!)
                }
            }
        val workId = intent.getStringExtra("workId")
        if (workId == null) finish()
        else {
            viewModel.getWorkDetails(workId)
            setContent {
                val work by viewModel.work.collectAsState(initial = idle())
                WorkDetailsView(
                    workState = work,
                    content = content,
                    onBackRequested = { finish() },
                    onLogSelected = { logId ->
                        Log.v(ContentValues.TAG, "Log selected: $logId")
                        viewModel.getLog(logId)
                    },
                    onLogBackRequested = {
                        viewModel.clearSelectedLog()
                    },
                    onEditUploadRequested = {
                        flag = true
                        openGallery()
                    },
                    onUploadRequested = {
                        flag = false
                        content = it
                        openGallery()
                    },
                    onRemoveRequested = { txt, fileName ->
                        content = txt
                        selectedFiles.remove(fileName)
                        viewModel.updateFiles(selectedFiles)
                    },
                    onCreateClicked = {
                        viewModel.createLog(workId, LogInputModel(it, selectedFiles))
                    },
                    onDeleteSubmit = { fileId, fileType ->
                        viewModel.deleteFile(fileId, fileType)
                    },
                    onEditSubmit = {
                        viewModel.editLog(it)
                    }
                )
                work.let {
                    if (it is Loaded && it.value.isFailure) {
                        val msg = it.value.exceptionOrNull()?.message ?: "Something went wrong"
                        ErrorAlert(
                            title = "Ups!",
                            message = msg,
                            buttonText = "Ok",
                            onDismiss = {
                                viewModel.resetToIdle()
                                if (msg == "Login Required") {
                                    viewModel.clearUser()
                                    LoginActivity.navigateTo(this)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.v(ContentValues.TAG, "WorkDetailsActivity.onStart() called")
        val workId = intent.getStringExtra("workId")
        if (workId == null) finish()
        else viewModel.getWorkDetails(workId)
    }

    private fun openGallery() {
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

    override fun onResume() {
        super.onResume()
        Log.v(ContentValues.TAG, "WorkDetailsActivity.onResume() called")
        if (flag) {
            Log.v(ContentValues.TAG, "EditUpload called")
            viewModel.uploadFiles(selectedFiles)
        } else {
            Log.v(ContentValues.TAG, "Update called")
            viewModel.updateFiles(selectedFiles)
        }
    }

}
