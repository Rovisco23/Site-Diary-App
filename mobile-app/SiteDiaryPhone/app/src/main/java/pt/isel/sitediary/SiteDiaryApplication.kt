package pt.isel.sitediary

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import okhttp3.OkHttpClient
import pt.isel.sitediary.infrastructure.LoggedUserDataStore
import pt.isel.sitediary.infrastructure.LoggedUserRepository
import pt.isel.sitediary.services.LogService
import pt.isel.sitediary.services.UserService
import pt.isel.sitediary.services.WorkService

interface DependenciesContainer {
    val loggedUserRepository: LoggedUserRepository
}

class SiteDiaryApplication : Application(), DependenciesContainer {
    private val httpClient: OkHttpClient = OkHttpClient.Builder().build()
    private val gson: Gson = Gson()
    private val url = "https://9d23-2001-818-d9f6-e100-9486-e29e-bb8d-41b7.ngrok-free.app"
    val userService = UserService(httpClient, gson, url)
    val workService = WorkService(httpClient, gson, url)
    val logService = LogService(httpClient, gson, url)
    private val dataStore: DataStore<Preferences> by preferencesDataStore(name = "data_store")
    override val loggedUserRepository: LoggedUserRepository get() = LoggedUserDataStore(dataStore)
}