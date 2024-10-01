package pt.isel.sitediary.repository.mappers

import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.sitediary.domainmodel.work.OwnLogSimplified
import java.sql.Date
import java.sql.ResultSet
import java.util.*

class OwnLogSimplifiedMapper : RowMapper<OwnLogSimplified> {
    override fun map(rs: ResultSet?, ctx: StatementContext?): OwnLogSimplified? = if (rs != null) {
        OwnLogSimplified(
            id = rs.getInt("id"),
            workId = UUID.fromString(rs.getString("oId")),
            workName = rs.getString("nome"),
            author = rs.getString("author"),
            editable = rs.getBoolean("editable"),
            attachments = rs.getBoolean("attachments"),
            createdAt = Date.valueOf(rs.getString("createdAt"))
        )
    } else null
}
