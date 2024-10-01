package pt.isel.sitediary.repository.mappers

import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.sitediary.domainmodel.work.ConstructionCompany
import pt.isel.sitediary.domainmodel.work.OpeningTermLocation
import pt.isel.sitediary.domainmodel.work.OpeningTermVerification
import pt.isel.sitediary.domainmodel.work.SiteDiary
import pt.isel.sitediary.domainmodel.work.SiteDiaryLog
import java.sql.ResultSet

class SiteDiaryMapper : RowMapper<SiteDiary> {
    override fun map(rs: ResultSet?, ctx: StatementContext?): SiteDiary? = if (rs != null) {
        val technicians = makeMap(rs.getString("technicians").removeSurrounding("{", "}"))
        val logs = makeList(rs.getString("logs").removeSurrounding("{", "}"))
        SiteDiary(
            verification = OpeningTermVerification(
                doc = rs.getString("autorizacao") ?: "",
                signature = rs.getString("assinatura") ?: "",
                dt_signature = rs.getString("dt_assinatura") ?: ""
            ),
            location = OpeningTermLocation(
                county = rs.getString("concelho"),
                parish = rs.getString("freguesia"),
                street = rs.getString("rua"),
                postalCode = rs.getString("cpostal"),
                building = rs.getString("predio")
            ),
            licenseHolder = rs.getString("titular_licenca"),
            authors = technicians,
            company = ConstructionCompany(
                name = rs.getString("nome_empresa"),
                num = rs.getInt("numero_empresa")
            ),
            type = rs.getString("tipo"),
            logs = logs
        )
    } else null
}

private fun makeList(str: String): List<SiteDiaryLog> {
    val list = mutableListOf<SiteDiaryLog>()
    if (str.isEmpty()) return list
    val split = str.split(",")
    split.map {
            val aux = it.removeSurrounding('"'.toString(), '"'.toString()).split(";")
            list.add(
                SiteDiaryLog(
                    content = aux[0],
                    author = aux[1],
                    createdAt = aux[2].split(".")[0].dropLast(3),
                    lastModificationAt = aux[3].split(".")[0].dropLast(3)
                )
            )
        }
    return list
}