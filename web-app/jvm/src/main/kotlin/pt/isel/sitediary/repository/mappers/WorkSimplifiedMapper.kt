package pt.isel.sitediary.repository.mappers

import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.sitediary.domainmodel.work.Address
import pt.isel.sitediary.domainmodel.work.Location
import pt.isel.sitediary.domainmodel.work.WorkSimplified
import java.sql.ResultSet
import java.util.*

class WorkSimplifiedMapper : RowMapper<WorkSimplified> {
    override fun map(rs: ResultSet?, ctx: StatementContext?): WorkSimplified? = if (rs != null) {
        WorkSimplified(
            id = UUID.fromString(rs.getString("id")),
            name = rs.getString("nome"),
            description = rs.getString("descricao"),
            type = rs.getString("tipo"),
            state = rs.getString("estado"),
            owner = rs.getString("owner"),
            address = Address(
                location = Location(
                    rs.getString("distrito"),
                    rs.getString("concelho"),
                    rs.getString("freguesia")
                ),
                rs.getString("rua"),
                rs.getString("cpostal")
            ),
            verification = rs.getBoolean("verification")
        )
    } else null
}