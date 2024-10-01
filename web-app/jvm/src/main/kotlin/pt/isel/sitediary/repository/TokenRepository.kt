package pt.isel.sitediary.repository

import kotlinx.datetime.Instant
import pt.isel.sitediary.domainmodel.authentication.Token
import pt.isel.sitediary.domainmodel.authentication.TokenValidationInfo

interface TokenRepository {
    fun createToken(token: Token, maxTokens: Int)
    fun updateLastUsedToken(token: Token, now: Instant)
    fun checkSession(userId: Int, token: String): Boolean
    fun deleteToken(token: TokenValidationInfo) : Boolean
}