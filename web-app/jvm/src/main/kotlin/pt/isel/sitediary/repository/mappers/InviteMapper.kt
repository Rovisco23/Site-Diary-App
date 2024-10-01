package pt.isel.sitediary.repository.mappers

import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.sitediary.domainmodel.work.Invite
import java.sql.ResultSet
import java.util.UUID

class InviteMapper : RowMapper<Invite> {

    override fun map(rs: ResultSet?, ctx: StatementContext?): Invite? = if (rs != null) {
        Invite(
            UUID.fromString(rs.getString("id")),
            rs.getString("email"),
            rs.getString("role"),
            UUID.fromString(rs.getString("oId"))
        )
    } else null
}