package pt.isel.sitediary.infrastructure

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import pt.isel.sitediary.domain.LoggedUser

private const val ID_KEY = "id"
private const val USERNAME_KEY = "username"
private const val TOKEN_KEY = "token"
private const val ROLE_KEY = "role"

class LoggedUserDataStore(private val store: DataStore<Preferences>) : LoggedUserRepository {

    private val idKey = intPreferencesKey(ID_KEY)
    private val usernameKey = stringPreferencesKey(USERNAME_KEY)
    private val tokenKey = stringPreferencesKey(TOKEN_KEY)
    private val roleKey = stringPreferencesKey(ROLE_KEY)

    override suspend fun getUserInfo(): LoggedUser? {
        val preferences = store.data.first()
        val id = preferences[idKey]
        val username = preferences[usernameKey]
        val token = preferences[tokenKey]
        val role = preferences[roleKey]
        return if (id != null && username != null && token != null && role != null) LoggedUser(
            id,
            username,
            token,
            role
        ) else null
    }

    override suspend fun updateUserInfo(loggedUser: LoggedUser?) {
        loggedUser?.let {
            store.edit { preferences ->
                preferences[idKey] = it.userId
                preferences[usernameKey] = it.username
                preferences[tokenKey] = it.token
                preferences[roleKey] = it.role
            }
        }
    }

    override suspend fun clearUserInfo() {
        store.edit { preferences ->
            preferences.remove(idKey)
            preferences.remove(usernameKey)
            preferences.remove(tokenKey)
            preferences.remove(roleKey)
        }
    }
}