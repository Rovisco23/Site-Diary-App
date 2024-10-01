package pt.isel.sitediary.domainmodel.work

import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.SimpleEmail
import pt.isel.sitediary.domainmodel.user.Member
import pt.isel.sitediary.domainmodel.user.Technician
import pt.isel.sitediary.domainmodel.user.User
import java.util.*

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
    val log: List<LogEntrySimplified>,
    val technicians: List<Technician>,
    val verification: Boolean,
    val verificationDoc: String?,
    val images: Int,
    val docs: Int
) {

    fun checkCouncil(user: User) = address.location.parish == user.location.parish &&
            address.location.county == user.location.county &&
            address.location.district == user.location.district

    fun sendEmailInvitation(invite: Invite) {
        val mail = SimpleEmail()
        mail.hostName = "smtp.googlemail.com"
        mail.setSmtpPort(465)
        mail.setAuthenticator(DefaultAuthenticator("sitediaryteam@gmail.com", "aozx lmoh yban imfz"))
        mail.isSSLOnConnect = true
        mail.setFrom("sitediaryteam@gmail.com", "SiteDiary")
        mail.addTo(invite.email)
        mail.subject = "Convite para a obra $name"
        val acceptLink = "http://localhost:4200/invites/$id"
        val role = if (invite.role != "MEMBRO" && invite.role != "VIEWER") "Técnico de ${invite.role}" else invite.role
        mail.setMsg(
            "Olá\n\nFoi convidado para a obra $name para participar como ${role}.\n\n" +
                    "Clique no link para aceitar o convite: $acceptLink\n\n" +
                    "Cumprimentos,\nA equipa da SiteDiary"
        )
        mail.send()
    }
}

data class WorkInput(
    val id: UUID,
    val name: String,
    val description: String,
    val type: WorkType,
    val state: WorkState,
    val address: Address,
    val members: List<Member>,
    val log: List<LogEntrySimplified>
)

data class AskVerificationInputModel(
    val workId: UUID,
    val verificationDoc: String
)

data class Author(
    val id: Int,
    val name: String,
    val role: String
)

data class WorkSimplified(
    val id: UUID,
    val owner: String,
    val name: String,
    val description: String,
    val type: String,
    val state: String,
    val address: Address,
    val verification: Boolean
)

data class WorkVerifying(
    val id: UUID,
    val owner: String,
    val name: String,
    val type: String,
    val address: Address,
)

data class ConstructionCompany(
    val name: String,
    val num: Int
)

data class Association(
    val name: String?,
    val number: Int?
)

data class MemberProfile(
    val id: Int,
    val name: String,
    val role: String,
    val email: String,
    val phone: String?,
    val location: Location
)

enum class WorkType(val description: String) {
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

enum class WorkState(val description: String) {
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

data class Invite(val id: UUID, val email: String, val role: String, val workId: UUID)

data class InviteSimplified(val workId: UUID, val workTitle: String, val role: String, val admin: String){
    fun checkTechnician() = role != "MEMBRO" && role != "ESPECTADOR"
}
