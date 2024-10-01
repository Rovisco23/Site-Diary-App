package pt.isel.sitediary.repository.mappers

import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.sitediary.domainmodel.work.Association
import pt.isel.sitediary.domainmodel.work.Location
import pt.isel.sitediary.model.GetUserModel
import java.sql.ResultSet

class GetUserMapper : RowMapper<GetUserModel> {

    override fun map(rs: ResultSet?, ctx: StatementContext?): GetUserModel? = if (rs != null) {
        GetUserModel(
            id = rs.getInt("id"),
            username = rs.getString("username"),
            nif = rs.getInt("nif"),
            email = rs.getString("email"),
            phone = rs.getString("telefone") ?: "",
            firstName = rs.getString("nome"),
            lastName = rs.getString("apelido"),
            role = rs.getString("role"),
            association = Association(
                name = rs.getString("associacao_nome"),
                number = rs.getInt("associacao_numero")
            ),
            location = Location(
                rs.getString("distrito"),
                rs.getString("concelho"),
                rs.getString("freguesia")
            )
        )
    } else null
}
