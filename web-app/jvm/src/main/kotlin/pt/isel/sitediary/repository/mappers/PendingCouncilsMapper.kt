package pt.isel.sitediary.repository.mappers

import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.sitediary.domainmodel.work.Association
import pt.isel.sitediary.domainmodel.work.Location
import pt.isel.sitediary.model.PendingCouncils
import java.sql.ResultSet

class PendingCouncilsMapper : RowMapper<PendingCouncils> {

    override fun map(rs: ResultSet?, ctx: StatementContext?): PendingCouncils? = if (rs != null) {
        PendingCouncils(
            id = rs.getInt("id"),
            name = rs.getString("nome") + " " + rs.getString("apelido"),
            email = rs.getString("email"),
            nif = rs.getInt("nif"),
            location = Location(
                district = rs.getString("distrito"),
                county = rs.getString("concelho"),
                parish = rs.getString("freguesia")
            ),
            username = rs.getString("username"),
            association = Association(
                name = rs.getString("associacao_nome"),
                number = rs.getInt("associacao_numero")
            )
        )
    } else null
}