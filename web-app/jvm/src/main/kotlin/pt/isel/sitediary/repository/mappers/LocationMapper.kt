package pt.isel.sitediary.repository.mappers

import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.sitediary.domainmodel.work.Location
import java.sql.ResultSet

class LocationMapper : RowMapper<Location> {

    override fun map(rs: ResultSet?, ctx: StatementContext?): Location? = if (rs != null) {
        Location(
            rs.getString("distrito"),
            rs.getString("concelho"),
            rs.getString("freguesia")
            )
    } else null

}