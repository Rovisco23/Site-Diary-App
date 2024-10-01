package pt.isel.sitediary.domainmodel.authentication

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.*

@Component
class UsersDomain(
    //private val passwordEncoder: PasswordEncoder,
    private val tokenEncoder: TokenEncoder,
    private val config: UsersDomainConfig
) {

    fun hashPassword(password: String): String = tokenEncoder.hashPassword(password)

    //fun encodePassword(password: String): String = passwordEncoder.encode(password)

    //fun validatePassword(password: String, encodedPassword: String): Boolean = passwordEncoder.matches(password, encodedPassword)

    fun generateTokenValue(): String =
        ByteArray(config.tokenSizeInBytes).let { byteArray ->
            SecureRandom.getInstanceStrong().nextBytes(byteArray)
            Base64.getUrlEncoder().encodeToString(byteArray)
        }

    fun canBeToken(token: String): Boolean = try {
        Base64.getUrlDecoder()
            .decode(token).size == config.tokenSizeInBytes
    } catch (ex: IllegalArgumentException) {
        false
    }

    fun isTokenTimeValid(
        clock: Clock,
        token: Token
    ): Boolean {
        val now = clock.now()
        return token.createdAt <= now &&
                (now - token.createdAt) <= config.tokenTtl &&
                (now - token.lastUsedAt) <= config.tokenRollingTtl
    }

    fun getTokenExpiration(token: Token): Instant {
        val absoluteExpiration = token.createdAt + config.tokenTtl
        val rollingExpiration = token.lastUsedAt + config.tokenRollingTtl
        return if (absoluteExpiration < rollingExpiration) {
            absoluteExpiration
        } else {
            rollingExpiration
        }
    }

    fun createTokenValidationInformation(token: String): TokenValidationInfo =
        tokenEncoder.createValidationInformation(token)

    fun isSafePassword(password: String) =
        password.length > 8 && password.any { !it.isLetterOrDigit() } &&
                password.any { it.isDigit() } && password.any { it.isUpperCase() }

    val maxNumberOfTokensPerUser = config.maxTokensPerUser
}