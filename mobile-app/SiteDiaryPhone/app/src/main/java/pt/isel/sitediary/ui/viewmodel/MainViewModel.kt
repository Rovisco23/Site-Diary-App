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
import pt.isel.sitediary.domain.Profile
import pt.isel.sitediary.domain.MainValues
import pt.isel.sitediary.domain.UploadInput
import pt.isel.sitediary.domain.getOrNull
import pt.isel.sitediary.domain.idle
import pt.isel.sitediary.domain.loaded
import pt.isel.sitediary.domain.loading
import pt.isel.sitediary.infrastructure.LoggedUserRepository
import pt.isel.sitediary.services.LogService
import pt.isel.sitediary.services.UserService
import pt.isel.sitediary.services.WorkService
import pt.isel.sitediary.ui.common.EditProfileException
import pt.isel.sitediary.ui.common.GetMainActivityValuesException
import pt.isel.sitediary.ui.common.LogException
import java.io.File

class MainViewModel(
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
            initializer { MainViewModel(workService, logService, userService, repo) }
        }
    }

    private val _mainValuesFlow: MutableStateFlow<LoadState<MainValues>> =
        MutableStateFlow(idle())

    val mainValues: Flow<LoadState<MainValues>>
        get() = _mainValuesFlow.asStateFlow()

    fun getAllValues() {
        if (_mainValuesFlow.value is Idle) {
            _mainValuesFlow.value = loading()
            viewModelScope.launch {
                try {
                    val loggedUser =
                        repo.getUserInfo() ?: throw GetMainActivityValuesException("Login Required")
                    val workList = workService.getAllWork(loggedUser.token)
                    val user = userService.getProfile(loggedUser.userId, loggedUser.token)
                    val profilePicture =
                        userService.getProfilePicture(loggedUser.userId, loggedUser.token)
                    val logs = logService.getAllLogs(loggedUser.token)
                    val rsp = MainValues(
                        workList = workList,
                        logs = logs,
                        profile = Profile(user, profilePicture)
                    )
                    _mainValuesFlow.value = loaded(Result.success(rsp))
                } catch (e: GetMainActivityValuesException) {
                    val msg = e.message ?: "Something went wrong"
                    _mainValuesFlow.value =
                        loaded(Result.failure(GetMainActivityValuesException(msg, e)))
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                val oldValues = _mainValuesFlow.value.getOrNull()
                    ?: throw GetMainActivityValuesException("Something went wrong")
                _mainValuesFlow.value = loading()
                val loggedUser =
                    repo.getUserInfo() ?: throw GetMainActivityValuesException("Login Required")
                val workList = workService.getAllWork(loggedUser.token)
                val logs = logService.getAllLogs(loggedUser.token)
                _mainValuesFlow.value =
                    loaded(Result.success(oldValues.copy(workList = workList, logs = logs)))
            } catch (e: GetMainActivityValuesException) {
                val msg = e.message ?: "Something went wrong"
                _mainValuesFlow.value =
                    loaded(Result.failure(GetMainActivityValuesException(msg, e)))
            }
        }
    }

    fun editProfilePicture(selectedFile: HashMap<String, File>) {
        if (_mainValuesFlow.value is Loaded) {
            viewModelScope.launch {
                try {
                    val oldValues = _mainValuesFlow.value.getOrNull()
                        ?: throw EditProfileException("Something went wrong")
                    _mainValuesFlow.value = loading()
                    val loggedUser =
                        repo.getUserInfo() ?: throw EditProfileException("Login Required")
                    val key = selectedFile.keys.first()
                    val upload = userService.editProfilePicture(
                        key,
                        selectedFile[key]!!,
                        loggedUser.token
                    )
                    val profilePicture =
                        userService.getProfilePicture(loggedUser.userId, loggedUser.token)
                    _mainValuesFlow.value = loaded(
                        Result.success(
                            oldValues.copy(
                                profile = oldValues.profile.copy(profilePicture = profilePicture)
                            )
                        )
                    )
                } catch (e: EditProfileException) {
                    val msg = e.message ?: "Something went wrong"
                    _mainValuesFlow.value =
                        loaded(Result.failure(EditProfileException(msg, e)))
                }
            }
        }
    }

    fun getLog(logId: Int) {
        viewModelScope.launch {
            try {
                val oldValues = _mainValuesFlow.value.getOrNull()
                    ?: throw LogException("Something went wrong")
                _mainValuesFlow.value = loading()
                val token = repo.getUserInfo()?.token ?: throw LogException("Login Required")
                val log = logService.getLogById(logId, token)
                _mainValuesFlow.value = loaded(Result.success(oldValues.copy(selectedLog = log)))
            } catch (e: LogException) {
                val msg = e.message ?: "Something went wrong"
                _mainValuesFlow.value = loaded(Result.failure(LogException(msg, e)))
            }
        }
    }

    fun clearSelectedLog() {
        viewModelScope.launch {
            try {
                val oldValues = _mainValuesFlow.value.getOrNull()
                    ?: throw LogException("Something went wrong")
                _mainValuesFlow.value = loading()
                _mainValuesFlow.value = loaded(Result.success(oldValues.copy(selectedLog = null)))
            } catch (e: LogException) {
                val msg = e.message ?: "Something went wrong"
                _mainValuesFlow.value = loaded(Result.failure(LogException(msg, e)))
            }
        }
    }

    fun uploadFiles(selectedFiles: HashMap<String, File>) {
        viewModelScope.launch {
            if (_mainValuesFlow.value is Loaded) {
                val oldValues = _mainValuesFlow.value.getOrNull()
                    ?: throw LogException("Something went wrong")
                _mainValuesFlow.value = loading()
                try {
                    val token = repo.getUserInfo()?.token ?: throw LogException("Login Required")
                    val update = logService.uploadFiles(
                        UploadInput(
                            oldValues.selectedLog!!.id,
                            oldValues.selectedLog.workId.toString(),
                            oldValues.selectedLog.content,
                            selectedFiles
                        ),
                        token
                    )
                    val logs = logService.getAllLogs(token)
                    val log = logService.getLogById(oldValues.selectedLog.id, token)
                    _mainValuesFlow.value =
                        loaded(Result.success(oldValues.copy(logs = logs, selectedLog = log)))
                    selectedFiles.clear()
                } catch (e: LogException) {
                    val msg = e.message ?: "Something went wrong"
                    _mainValuesFlow.value = loaded(Result.failure(LogException(msg, e)))
                }
            }
        }
    }

    fun deleteFile(fileId: Int, fileType: String) {
        viewModelScope.launch {
            val oldValues = _mainValuesFlow.value.getOrNull()
                ?: throw LogException("Something went wrong")
            _mainValuesFlow.value = loading()
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
                val logs = logService.getAllLogs(token)
                val log = logService.getLogById(oldValues.selectedLog.id, token)
                _mainValuesFlow.value =
                    loaded(Result.success(oldValues.copy(logs = logs, selectedLog = log)))
            } catch (e: LogException) {
                val msg = e.message ?: "Something went wrong"
                _mainValuesFlow.value = loaded(Result.failure(LogException(msg, e)))
            }
        }
    }

    fun editLog(content: String) {
        viewModelScope.launch {
            val oldValues = _mainValuesFlow.value.getOrNull()
                ?: throw LogException("Something went wrong")
            _mainValuesFlow.value = loading()
            try {
                val token = repo.getUserInfo()?.token ?: throw LogException("Login Required")
                val upload = logService.editLog(
                    oldValues.selectedLog!!.id,
                    oldValues.selectedLog.workId.toString(),
                    content,
                    token
                )
                val logs = logService.getAllLogs(token)
                val log = logService.getLogById(oldValues.selectedLog.id, token)
                _mainValuesFlow.value =
                    loaded(Result.success(oldValues.copy(logs = logs, selectedLog = log)))
            } catch (e: LogException) {
                val msg = e.message ?: "Something went wrong"
                _mainValuesFlow.value = loaded(Result.failure(LogException(msg, e)))
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repo.clearUserInfo()
        }
    }

    fun resetToIdle() {
        if (_mainValuesFlow.value !is Idle) {
            _mainValuesFlow.value = idle()
        }
    }
}