package pt.isel.sitediary.repository.mappers


import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.sitediary.domainmodel.work.ConstructionCompany
import pt.isel.sitediary.domainmodel.work.OpeningTerm
import pt.isel.sitediary.domainmodel.work.OpeningTermAuthor
import pt.isel.sitediary.domainmodel.work.OpeningTermLocation
import pt.isel.sitediary.domainmodel.work.OpeningTermVerification
import pt.isel.sitediary.service.NO_INPUT
import java.sql.ResultSet

class OpeningTermMapper : RowMapper<OpeningTerm> {
    override fun map(rs: ResultSet?, ctx: StatementContext?): OpeningTerm? = if (rs != null) {
        val technicians = makeMap(rs.getString("technicians").removeSurrounding("{", "}"))
        OpeningTerm(
            verification = OpeningTermVerification(
                doc = rs.getString("autorizacao") ?: NO_INPUT,
                signature = rs.getString("assinatura") ?: NO_INPUT,
                dt_signature = (rs.getString("dt_assinatura") ?: NO_INPUT).split(".")[0].dropLast(3)
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
            type = rs.getString("tipo")
        )
    } else null
}

fun makeMap(str: String): Map<String, OpeningTermAuthor> {
    val map = mutableMapOf<String, OpeningTermAuthor>()
    str.split(",")
        .map {
            val aux = it.removeSurrounding('"'.toString(), '"'.toString()).split(";")
            map[mapToAuthor(aux[0])] = OpeningTermAuthor(
                name = aux[1],
                association = aux[2],
                num = aux[3].toInt()
            )
        }
    return map
}

fun mapToAuthor(str: String): String = when (str) {
    "DIRETOR" -> "director"
    "FISCALIZAÇÃO" -> "fiscalization"
    "COORDENADOR" -> "coordinator"
    "ARQUITETURA" -> "architect"
    "ESTABILIDADE" -> "stability"
    "ELETRICIDADE" -> "electricity"
    "GÁS" -> "gas"
    "CANALIZAÇÃO" -> "water"
    "TELECOMUNICAÇÕES" -> "phone"
    "TERMICO" -> "isolation"
    "ACUSTICO" -> "acoustic"
    "TRANSPORTES" -> "transport"
    else -> throw IllegalArgumentException("Invalid author type")
}