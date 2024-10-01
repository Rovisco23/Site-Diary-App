package pt.isel.sitediary.repository.mappers

import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.sitediary.model.FileModel
import java.sql.ResultSet

class FileModelMapper : RowMapper<FileModel> {
    override fun map(rs: ResultSet?, ctx: StatementContext?): FileModel? = if (rs != null) {
        FileModel(
            file = rs.getBytes("file"),
            fileName = rs.getString("name"),
            contentType = rs.getString("type")
        )
    } else null
}