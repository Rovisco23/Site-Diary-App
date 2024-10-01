package pt.isel.sitediary.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import pt.isel.sitediary.SiteDiaryApplication
import pt.isel.sitediary.domain.Loaded
import pt.isel.sitediary.domain.idle
import pt.isel.sitediary.ui.common.ErrorAlert
import pt.isel.sitediary.ui.viewmodel.UserViewModel
import pt.isel.sitediary.ui.main.MainActivity
import android.content.ContentValues
import android.util.Log


class LoginActivity : ComponentActivity() {

    private val app by lazy { application as SiteDiaryApplication }
    private val viewModel by viewModels<UserViewModel>(
        factoryProducer = { UserViewModel.factory(app.userService, app.loggedUserRepository) }
    )

    companion object {
        fun navigateTo(origin: ComponentActivity) {
            val intent = Intent(origin, LoginActivity::class.java)
            origin.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(ContentValues.TAG, "LoginActivity.onCreate() called")
        setContent {
            val authUser by viewModel.loggedUser.collectAsState(initial = idle())
            LoginScreen(
                onLogin = { input -> viewModel.login(input.username, input.password) }
            )
            authUser.let {
                if (it is Loaded && it.value.isFailure) {
                    ErrorAlert(
                        title = "Ups!",
                        message = it.value.exceptionOrNull()?.message ?: "Something went wrong",
                        buttonText = "Ok",
                        onDismiss = { viewModel.resetToIdle() }
                    )
                } else if (it is Loaded && it.value.isSuccess) {
                    viewModel.resetToIdle()
                    MainActivity.navigateTo(this)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.v(ContentValues.TAG, "LoginActivity.onStart() called")
        viewModel.checkIfUserLogged()
    }
}