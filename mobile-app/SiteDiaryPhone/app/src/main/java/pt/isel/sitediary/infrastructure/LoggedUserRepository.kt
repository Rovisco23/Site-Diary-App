package pt.isel.sitediary.infrastructure

import pt.isel.sitediary.domain.LoggedUser

interface LoggedUserRepository {
    suspend fun getUserInfo(): LoggedUser?
    suspend fun updateUserInfo(loggedUser: LoggedUser?)
    suspend fun clearUserInfo()
}