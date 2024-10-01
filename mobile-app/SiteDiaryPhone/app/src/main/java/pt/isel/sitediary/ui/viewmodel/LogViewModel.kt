package pt.isel.sitediary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.isel.sitediary.domain.Idle
import pt.isel.sitediary.domain.LoadState
import pt.isel.sitediary.domain.Loading
import pt.isel.sitediary.domain.LogInputModel
import pt.isel.sitediary.domain.idle
import pt.isel.sitediary.domain.loaded
import pt.isel.sitediary.infrastructure.LoggedUserRepository
import pt.isel.sitediary.services.LogService
import pt.isel.sitediary.ui.common.LogCreationException

class LogViewModel(
    private val logService: LogService,
    private val repo: LoggedUserRepository
) : ViewModel() {

    companion object {
        fun factory(
            logService: LogService,
            repo: LoggedUserRepository
        ) = viewModelFactory {
            initializer { LogViewModel(logService, repo) }
        }
    }

    private val _creationFlow: MutableStateFlow<LoadState<Unit>> =
        MutableStateFlow(idle())

    val creation: Flow<LoadState<Unit>>
        get() = _creationFlow.asStateFlow()

    fun createLog(input: LogInputModel, workId: String) {
        if (_creationFlow.value is Idle) {
            _creationFlow.value = Loading
            viewModelScope.launch {
                try {
                    val loggedUser =
                        repo.getUserInfo() ?: throw LogCreationException("Login Required")
                    val aux = logService.createLog(
                        input,
                        workId,
                        loggedUser.token
                    )
                    _creationFlow.value = loaded(Result.success(aux))
                } catch (e: LogCreationException) {
                    val msg = e.message ?: "Something went wrong"
                    _creationFlow.value = loaded(Result.failure(LogCreationException(msg)))
                }
            }
        }
    }

    fun resetToIdle() {
        if (_creationFlow.value !is Idle) {
            _creationFlow.value = idle()
        }
    }

    fun clearUser() {
        viewModelScope.launch {
            repo.clearUserInfo()
        }
    }
}