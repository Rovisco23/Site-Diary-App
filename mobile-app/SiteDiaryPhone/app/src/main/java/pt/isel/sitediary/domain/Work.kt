package pt.isel.sitediary.domain

import java.io.File
import java.util.UUID

data class WorkListDto(
    val id: String,
    val name: String,
    val owner: String,
    val state: String,
    val address: Address,
    val verification: String
)

fun Array<WorkListDto>.toWorkSimplifiedList() = map {
    WorkSimplified(
        UUID.fromString(it.id),
        it.name,
        it.owner,
        WorkState.fromString(it.state)!!,
        it.address,
        it.verification.toBoolean()
    )
}

data class WorkSimplified(
    val id: UUID,
    val name: String,
    val owner: String,
    val state: WorkState,
    val address: Address,
    val verification: Boolean
)

data class Work(
    val id: UUID,
    val name: String,
    val description: String,
    val type: WorkType,
    val state: WorkState,
    val licenseHolder: String,
    val company: ConstructionCompany,
    val address: Address,
    val building: String,
    val members: List<Member>,
    val selectedMember: Profile? = null,
    val log: List<LogEntrySimplified>,
    val selectedLog: LogEntry? = null,
    val technicians: List<Technician>,
    val verification: Boolean,
    val images: Int,
    val docs: Int,
    val files: Map<String, File>? = null
) {
    fun toDetails() = WorkDetails(
        name,
        description,
        type,
        state,
        licenseHolder,
        company,
        address,
        building,
        technicians,
        log.size,
        images,
        docs
    )
}

data class WorkDetails(
    val name: String,
    val description: String,
    val type: WorkType,
    val state: WorkState,
    val licenseHolder: String,
    val company: ConstructionCompany,
    val address: Address,
    val building: String,
    val technicians: List<Technician>,
    val logs: Int,
    val images: Int,
    val docs: Int
)

data class ConstructionCompany(
    val name: String,
    val num: Int
) {
    override fun toString() = "$name - $num"
}

data class Association(
    val name: String,
    val number: Int
) {
    override fun toString() = "$name - $number"
}

data class OpeningTerm(
    val name: String,
    val type: WorkType,
    val licenseHolder: String,
    val technicians: List<Technician>,
    val constructionCompany: ConstructionCompany,
    val building: String
)

enum class WorkType(private val description: String) {
    RESIDENCIAL("RESIDENCIAL"),
    COMERCIAL("COMERCIAL"),
    INDUSTRIAL("INDUSTRIAL"),
    INFRAESTRUTURA("INFRAESTRUTURA"),
    INSTITUCIONAL("INSTITUCIONAL"),
    REABILITACAO("REABILITAÇÃO"),
    ESTRUTURA_ESPECIAL("ESTRUTURA ESPECIAL"),
    OBRA_DE_ARTE("OBRA DE ARTE"),
    HABITACAO("HABITAÇÃO"),
    EDIFICIOS_ESPECIAL("EDIFICIOS ESPECIAL");

    override fun toString() = description

    companion object {
        fun fromString(type: String) = when (type) {
            "RESIDENCIAL" -> RESIDENCIAL
            "COMERCIAL" -> COMERCIAL
            "INDUSTRIAL" -> INDUSTRIAL
            "INFRAESTRUTURA" -> INFRAESTRUTURA
            "INSTITUCIONAL" -> INSTITUCIONAL
            "REABILITAÇÃO" -> REABILITACAO
            "ESTRUTURA ESPECIAL" -> ESTRUTURA_ESPECIAL
            "OBRA DE ARTE" -> OBRA_DE_ARTE
            "HABITAÇÃO" -> HABITACAO
            "EDIFICIOS ESPECIAL" -> EDIFICIOS_ESPECIAL
            else -> null
        }
    }
}

enum class WorkState(private val description: String) {
    IN_PROGRESS("EM PROGRESSO"),
    FINISHED("TERMINADA"),
    REJECTED("REJEITADA"),
    VERIFYING("EM VERIFICAÇÃO");

    override fun toString() = description

    companion object {
        fun fromString(state: String) = when (state) {
            "EM PROGRESSO" -> IN_PROGRESS
            "TERMINADA" -> FINISHED
            "REJEITADA" -> REJECTED
            "EM VERIFICAÇÃO" -> VERIFYING
            else -> null
        }
    }
}