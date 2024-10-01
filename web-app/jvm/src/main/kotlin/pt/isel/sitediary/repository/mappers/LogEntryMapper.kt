package pt.isel.sitediary.repository.mappers

import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.sitediary.domainmodel.work.Author
import pt.isel.sitediary.domainmodel.work.LogEntry
import pt.isel.sitediary.model.FileOutputModel
import java.sql.Date
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.*

class LogEntryMapper : RowMapper<LogEntry> {
    override fun map(rs: ResultSet?, ctx: StatementContext?): LogEntry? = if (rs != null) {
        val modificationDate = rs.getTimestamp("last_modification_date")
        val documents = rs.getString("documents").removeSurrounding("{", "}")
        val images = rs.getString("images").removeSurrounding("{", "}")
        LogEntry(
            id = rs.getInt("id"),
            workId = UUID.fromString(rs.getString("oId")),
            content = rs.getString("texto"),
            editable = rs.getBoolean("editable"),
            createdAt = Date.from(rs.getTimestamp("creation_date").toInstant()),
            lastModifiedAt = Date.from(modificationDate.toInstant()),
            author = Author(
                id = rs.getInt("author"),
                name = rs.getString("username"),
                role = rs.getString("role")
            ),
            files = if (documents.isEmpty() && images.isEmpty()) emptyList() else {
                val files = mutableListOf<FileOutputModel>()
                if (documents.isNotEmpty()) {
                    documents.split(",")
                        .forEach {
                            val x = '"'.toString()
                            val aux = it.removeSurrounding(x, x).split(";")
                            files.add(
                                FileOutputModel(
                                    id = aux[0].toInt(),
                                    fileName = aux[1],
                                    contentType = aux[2],
                                    uploadDate = Date.from(Timestamp.valueOf(aux[3]).toInstant())
                                )
                            )
                        }
                }
                if (images.isNotEmpty()) {
                    images.split(",")
                        .map {
                            val x = '"'.toString()
                            val aux = it.removeSurrounding(x, x).split(";")
                            files.add(
                                FileOutputModel(
                                    id = aux[0].toInt(),
                                    fileName = aux[1],
                                    contentType = aux[2],
                                    uploadDate = Date.from(Timestamp.valueOf(aux[3]).toInstant())
                                )
                            )
                        }
                }
                files
            }
        )
    } else null
}
