package pt.isel.sitediary.repository.mappers

import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.sitediary.domainmodel.authentication.TokenValidationInfo
import pt.isel.sitediary.domainmodel.work.Association
import pt.isel.sitediary.domainmodel.work.Location
import pt.isel.sitediary.model.UserAndTokenModel
import java.sql.ResultSet

class UserAndTokenMapper : RowMapper<UserAndTokenModel> {

    override fun map(rs: ResultSet?, ctx: StatementContext?): UserAndTokenModel? = if (rs != null) {
        UserAndTokenModel(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("nome") + " " + rs.getString("apelido"),
            rs.getInt("nif"),
            rs.getString("email"),
            rs.getString("telefone") ?: "",
            rs.getString("role"),
            Location(
                rs.getString("distrito"),
                rs.getString("concelho"),
                rs.getString("freguesia")
            ),
            Association(
                rs.getString("associacao_nome"),
                rs.getInt("associacao_numero")
            ),
            TokenValidationInfo(rs.getString("token_validation")),
            rs.getLong("created_At"),
            rs.getLong("last_used_at")
        )
    } else null

}