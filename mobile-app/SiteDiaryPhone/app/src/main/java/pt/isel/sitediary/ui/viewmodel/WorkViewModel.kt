package pt.isel.sitediary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.isel.sitediary.domain.DeleteFileModel
import pt.isel.sitediary.domain.Idle
import pt.isel.sitediary.domain.LoadState
import pt.isel.sitediary.domain.Loaded
import pt.isel.sitediary.domain.LogInputModel
import pt.isel.sitediary.domain.Profile
import pt.isel.sitediary.domain.UploadInput
import pt.isel.sitediary.domain.Work
import pt.isel.sitediary.domain.getOrNull
import pt.isel.sitediary.domain.idle
import pt.isel.sitediary.domain.loaded
import pt.isel.sitediary.domain.loading
import pt.isel.sitediary.infrastructure.LoggedUserRepository
import pt.isel.sitediary.services.LogService
import pt.isel.sitediary.services.UserService
import pt.isel.sitediary.services.WorkService
import pt.isel.sitediary.ui.common.GetWorkException
import pt.isel.sitediary.ui.common.LogException
import pt.isel.sitediary.ui.common.ProfileException
import java.io.File

class WorkViewModel(
    private val workService: WorkService,
    private val logService: LogService,
    private val userService: UserService,
    private val repo: LoggedUserRepository
) : ViewModel() {

    companion object {
        fun factory(
            workService: WorkService,
            logService: LogService,
            userService: UserService,
            repo: LoggedUserRepository
        ) = viewModelFactory {
            initializer { WorkViewModel(workService, logService, userService, repo) }
        }
    }

    private val _workFlow: MutableStateFlow<LoadState<Work>> =
        MutableStateFlow(idle())

    val work: Flow<LoadState<Work>>
        get() = _workFlow.asStateFlow()

    fun getWorkDetails(workId: String) {
        if (_workFlow.value is Idle) {
            _workFlow.value = loading()
            viewModelScope.launch {
                try {
                    val token =
                        repo.getUserInfo()?.token ?: throw GetWorkException("Login Required")
                    val rsp = workService.getWork(workId, token)
                    _workFlow.value = loaded(Result.success(rsp))
                } catch (e: GetWorkException) {
                    val msg = e.message ?: "Something went wrong"
                    _workFlow.value = loaded(Result.failure(GetWorkException(msg, e)))
                }
            }
        }
    }

    fun getLog(logId: Int) {
        viewModelScope.launch {
            try {
                val oldValues = _workFlow.value.getOrNull()
                    ?: throw LogException("Something went wrong")
                _workFlow.value = loading()
                val token = repo.getUserInfo()?.token ?: throw LogException("Login Required")
                val log = logService.getLogById(logId, token)
                _workFlow.value = loaded(Result.success(oldValues.copy(selectedLog = log)))
            } catch (e: LogException) {
                val msg = e.message ?: "Something went wrong"
                _workFlow.value = loaded(Result.failure(LogException(msg, e)))
            }
        }
    }

    fun clearUser() {
        viewModelScope.launch {
            repo.clearUserInfo()
        }
    }

    fun resetToIdle() {
        if (_workFlow.value !is Loaded)
            throw IllegalStateException("The view model is not in the loaded state.")
        _workFlow.value = idle()
    }

    fun clearSelectedLog() {
        viewModelScope.launch {
            try {
                val oldValues = _workFlow.value.getOrNull()
                    ?: throw LogException("Something went wrong")
                _workFlow.value = loading()
                _workFlow.value = loaded(Result.success(oldValues.copy(selectedLog = null)))
            } catch (e: LogException) {
                val msg = e.message ?: "Something went wrong"
                _workFlow.value = loaded(Result.failure(LogException(msg, e)))
            }
        }
    }

    fun uploadFiles(selectedFiles: HashMap<String, File>) {
        viewModelScope.launch {
            if (_workFlow.value is Loaded) {
                val oldValues = _workFlow.value.getOrNull()
                    ?: throw LogException("Something went wrong")
                _workFlow.value = loading()
                try {
                    val token = repo.getUserInfo()?.token ?: throw LogException("Login Required")
                    val update = logService.uploadFiles(
                        UploadInput(
                            oldValues.selectedLog!!.id,
                            oldValues.id.toString(),
                            oldValues.selectedLog.content,
                            selectedFiles
                        ),
                        token
                    )
                    val work = workService.getWork(oldValues.id.toString(), token)
                    val log = logService.getLogById(oldValues.selectedLog.id, token)
                    _workFlow.value = loaded(Result.success(work.copy(selectedLog = log)))
                    selectedFiles.clear()
                } catch (e: LogException) {
                    val msg = e.message ?: "Something went wrong"
                    _workFlow.value = loaded(Result.failure(LogException(msg, e)))
                }
            }
        }
    }

    fun deleteFile(fileId: Int, fileType: String) {
        viewModelScope.launch {
            val oldValues = _workFlow.value.getOrNull()
                ?: throw LogException("Something went wrong")
            _workFlow.value = loading()
            try {
                val token = repo.getUserInfo()?.token ?: throw LogException("Login Required")
                val update = logService.deleteFile(
                    DeleteFileModel(
                        logId = oldValues.selectedLog!!.id,
                        fileId = fileId,
                        type = fileType
                    ),
                    token
                )
                val work = workService.getWork(oldValues.id.toString(), token)
                val log = logService.getLogById(oldValues.selectedLog.id, token)
                _workFlow.value = loaded(Result.success(work.copy(selectedLog = log)))
            } catch (e: LogException) {
                val msg = e.message ?: "Something went wrong"
                _workFlow.value = loaded(Result.failure(LogException(msg, e)))
            }
        }
    }


    fun createLog(workId: String, input: LogInputModel) {
        _workFlow.value = loading()
        viewModelScope.launch {
            try {
                val token = repo.getUserInfo()?.token ?: throw LogException("Login Required")
                val upload = logService.createLog(input, workId, token)
                val work = workService.getWork(workId, token)
                _workFlow.value = loaded(Result.success(work))
            } catch (e: LogException) {
                val msg = e.message ?: "Something went wrong"
                _workFlow.value = loaded(Result.failure(LogException(msg, e)))
            }
        }
    }

    fun updateFiles(selectedFiles: HashMap<String, File>) {
        if (_workFlow.value is Loaded) {
            viewModelScope.launch {
                val oldValues = _workFlow.value.getOrNull()
                    ?: throw LogException("Something went wrong")
                _workFlow.value = loading()
                try {
                    val token = repo.getUserInfo()?.token ?: throw LogException("Login Required")
                    _workFlow.value = loaded(Result.success(oldValues.copy(files = selectedFiles)))
                } catch (e: LogException) {
                    val msg = e.message ?: "Something went wrong"
                    _workFlow.value = loaded(Result.failure(LogException(msg, e)))
                }
            }
        }
    }

    fun editLog(content: String) {
        viewModelScope.launch {
            val oldValues = _workFlow.value.getOrNull()
                ?: throw LogException("Something went wrong")
            _workFlow.value = loading()
            try {
                val token = repo.getUserInfo()?.token ?: throw LogException("Login Required")
                val upload = logService.editLog(
                    oldValues.selectedLog!!.id,
                    oldValues.id.toString(),
                    content,
                    token
                )
                val work = workService.getWork(oldValues.id.toString(), token)
                val log = logService.getLogById(oldValues.selectedLog.id, token)
                _workFlow.value = loaded(Result.success(work.copy(selectedLog = log)))
            } catch (e: LogException) {
                val msg = e.message ?: "Something went wrong"
                _workFlow.value = loaded(Result.failure(LogException(msg, e)))
            }
        }
    }
}
