package pt.isel.sitediary.repository.mappers

import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.sitediary.domainmodel.work.Address
import pt.isel.sitediary.domainmodel.work.Location
import pt.isel.sitediary.domainmodel.work.WorkVerifying
import java.sql.ResultSet
import java.util.*

class WorkVerifyingMapper : RowMapper<WorkVerifying> {
    override fun map(rs: ResultSet?, ctx: StatementContext?): WorkVerifying? = if (rs != null) {
        WorkVerifying(
            id = UUID.fromString(rs.getString("id")),
            name = rs.getString("nome"),
            type = rs.getString("tipo"),
            owner = rs.getString("ownerFirstName") + " "+ rs.getString("ownerLastName"),
            address = Address(
                location = Location(
                    rs.getString("distrito"),
                    rs.getString("concelho"),
                    rs.getString("freguesia")
                ),
                rs.getString("rua"),
                rs.getString("cpostal")
            )
        )
    } else null
}