package pt.isel.sitediary.repository.mappers

import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.sitediary.domainmodel.work.Location
import pt.isel.sitediary.domainmodel.work.MemberProfile
import java.sql.ResultSet

class MemberProfileMapper : RowMapper<MemberProfile> {
    override fun map(rs: ResultSet?, ctx: StatementContext?): MemberProfile? = if (rs != null) {
        MemberProfile(
            id = rs.getInt("id"),
            name = rs.getString("nome") + " " + rs.getString("apelido"),
            email = rs.getString("email"),
            role = rs.getString("role"),
            phone = rs.getString("telefone"),
            location = Location(
                district = rs.getString("distrito"),
                county = rs.getString("concelho"),
                parish = rs.getString("freguesia")
            )
        )
    } else null
}