package pt.isel.sitediary.repository.jdbi

import org.jdbi.v3.core.Handle
import pt.isel.sitediary.repository.TokenRepository
import kotlinx.datetime.Instant
import pt.isel.sitediary.domainmodel.authentication.Token
import pt.isel.sitediary.domainmodel.authentication.TokenValidationInfo

class JdbiToken(private val handle: Handle) : TokenRepository {
    override fun createToken(token: Token, maxTokens: Int) {
        handle.createUpdate(
            "delete from sessao " +
                    " where uId = :user_id and token_validation in (select token_validation from sessao " +
                    "where uId = :user_id" +
                    " order by last_used_at desc offset :offset)"
        )
            .bind("user_id", token.userId)
            .bind("offset", if (maxTokens > 1) maxTokens - 1 else 1)
            .execute()

        handle.createUpdate(
            "insert into sessao(uId, token_validation, created_at, last_used_at)" +
                    " values (:user_id, :token_validation, :created_at, :last_used_at)"
        )
            .bind("user_id", token.userId)
            .bind("token_validation", token.tokenValidationInfo.validationInfo)
            .bind("created_at", token.createdAt.epochSeconds)
            .bind("last_used_at", token.lastUsedAt.epochSeconds)
            .execute()
    }

    override fun updateLastUsedToken(token: Token, now: Instant) {
        handle.createUpdate(
            "update sessao set last_used_at = :last_used_at where token_validation = :validation_information"
        )
            .bind("last_used_at", now.epochSeconds)
            .bind("validation_information", token.tokenValidationInfo.validationInfo)
            .execute()
    }

    override fun checkSession(userId: Int, token: String) = handle.createQuery(
        "select uid from sessao where uid = :user_id and token_validation = :token"
    )
        .bind("user_id", userId)
        .bind("token", token)
        .mapTo(Int::class.java)
        .singleOrNull() == userId

    override fun deleteToken(token: TokenValidationInfo) = handle.createUpdate(
        "delete from sessao where token_validation = :token_validation"
    )
        .bind("token_validation", token.validationInfo)
        .execute() == 1
}