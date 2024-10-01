package pt.isel.sitediary.repository.mappers


import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.sitediary.domainmodel.user.Member
import pt.isel.sitediary.domainmodel.user.Technician
import pt.isel.sitediary.domainmodel.work.Address
import pt.isel.sitediary.domainmodel.work.Association
import pt.isel.sitediary.domainmodel.work.Author
import pt.isel.sitediary.domainmodel.work.ConstructionCompany
import pt.isel.sitediary.domainmodel.work.Location
import pt.isel.sitediary.domainmodel.work.LogEntrySimplified
import pt.isel.sitediary.domainmodel.work.Work
import pt.isel.sitediary.domainmodel.work.WorkState
import pt.isel.sitediary.domainmodel.work.WorkType
import java.sql.Date
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.*

class WorkMapper : RowMapper<Work> {
    override fun map(rs: ResultSet?, ctx: StatementContext?): Work? = if (rs != null) {
        val log = rs.getString("log").removeSurrounding("{", "}")
        val technicians = rs.getString("technicians").removeSurrounding("{", "}")
        Work(
            id = UUID.fromString(rs.getString("id")),
            name = rs.getString("nome"),
            description = rs.getString("descricao"),
            type = WorkType.fromString(rs.getString("tipo")) ?: WorkType.RESIDENCIAL,
            state = WorkState.fromString(rs.getString("estado")) ?: WorkState.IN_PROGRESS,
            licenseHolder = rs.getString("titular_licenca"),
            company = ConstructionCompany(
                name = rs.getString("company_name"),
                num = rs.getInt("company_num")
            ),
            address = Address(
                location = Location(
                    rs.getString("distrito"),
                    rs.getString("concelho"),
                    rs.getString("freguesia")
                ),
                rs.getString("rua"),
                rs.getString("cpostal")
            ),
            building = rs.getString("predio"),
            members = rs.getString("membros").removeSurrounding("{", "}").split(",")
                .map {
                    val aux = it.split(";")
                    Member(
                        id = aux[0].toInt(),
                        name = aux[1],
                        role = aux[2]
                    )
                },
            log = if (log.isEmpty()) emptyList() else {
                log.split(",")
                    .map {
                        val x = '"'.toString()
                        val aux = it.removeSurrounding(x, x).split(";")
                        LogEntrySimplified(
                            id = aux[0].toInt(),
                            author = Author(
                                id = aux[1].toInt(),
                                name = aux[2],
                                role = aux[3]
                            ),
                            editable = aux[4] == "t",
                            attachments = aux[5] == "t",
                            createdAt = Date.from(Timestamp.valueOf(aux[6]).toInstant()),
                        )
                    }
            },
            technicians = if (technicians.isEmpty()) emptyList() else {
                technicians.split(",")
                    .map {
                        val x = '"'.toString()
                        val aux = it.removeSurrounding(x, x).split(";")
                        Technician(
                            name = aux[0],
                            email = aux[1],
                            role = aux[2],
                            association = Association(
                                name = aux[3],
                                number = aux[4].toInt()
                            )
                        )
                    }
            },
            verification = rs.getBoolean("verification"),
            verificationDoc = rs.getString("autorizacao"),
            images = rs.getInt("imagens"),
            docs = rs.getInt("documentos")
        )
    } else null
}
