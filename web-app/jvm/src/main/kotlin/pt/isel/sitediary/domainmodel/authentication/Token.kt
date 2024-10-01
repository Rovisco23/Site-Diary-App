package pt.isel.sitediary.domainmodel.authentication

import kotlinx.datetime.Instant

class Token(
    val tokenValidationInfo: TokenValidationInfo,
    val userId: Int,
    val createdAt: Instant,
    val lastUsedAt: Instant
)

data class TokenExternalInfo(
    val tokenValue: String,
    val userId: Int,
    val username: String,
    val role: String,
    val tokenExpiration: Instant
)