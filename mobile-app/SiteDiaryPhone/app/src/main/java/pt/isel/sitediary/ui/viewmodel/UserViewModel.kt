package pt.isel.sitediary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.isel.sitediary.ui.common.LoginException
import pt.isel.sitediary.domain.LoggedUser
import pt.isel.sitediary.domain.Idle
import pt.isel.sitediary.domain.LoadState
import pt.isel.sitediary.domain.Loaded
import pt.isel.sitediary.domain.idle
import pt.isel.sitediary.domain.loaded
import pt.isel.sitediary.domain.loading
import pt.isel.sitediary.infrastructure.LoggedUserRepository
import pt.isel.sitediary.services.UserService

class UserViewModel(private val service: UserService, private val repo: LoggedUserRepository) :
    ViewModel() {

    companion object {
        fun factory(service: UserService, repo: LoggedUserRepository) = viewModelFactory {
            initializer { UserViewModel(service, repo) }
        }
    }

    private val _loggedUserFlow: MutableStateFlow<LoadState<LoggedUser?>> = MutableStateFlow(idle())

    val loggedUser: Flow<LoadState<LoggedUser?>>
        get() = _loggedUserFlow.asStateFlow()

    fun login(username: String, password: String) {
        if (_loggedUserFlow.value !is Idle)
            throw IllegalStateException("The view model is not in the idle state.")

        _loggedUserFlow.value = loading()
        viewModelScope.launch {
            try {
                val rsp = service.login(username, password)
                repo.updateUserInfo(rsp)
                _loggedUserFlow.value = loaded(Result.success(rsp))
            } catch (e: LoginException) {
                val msg = e.message ?: "Something went wrong"
                _loggedUserFlow.value = loaded(Result.failure(LoginException(msg, e)))
            }
        }
    }

    fun resetToIdle() {
        if (_loggedUserFlow.value !is Loaded)
            throw IllegalStateException("The view model is not in the loaded state.")
        _loggedUserFlow.value = idle()
    }

    fun checkIfUserLogged() {
        if (_loggedUserFlow.value !is Idle)
            throw IllegalStateException("The view model is not in the idle state.")

        _loggedUserFlow.value = loading()
        viewModelScope.launch {
            val rsp = repo.getUserInfo()
            if (rsp != null) _loggedUserFlow.value = loaded(Result.success(rsp))
            else _loggedUserFlow.value = idle()
        }
    }
}