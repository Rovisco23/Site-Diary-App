package pt.isel.sitediary.service

import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.springframework.stereotype.Component
import pt.isel.sitediary.domainmodel.user.User
import pt.isel.sitediary.domainmodel.user.checkOwner
import pt.isel.sitediary.domainmodel.user.containsMemberById
import pt.isel.sitediary.domainmodel.work.Address
import pt.isel.sitediary.domainmodel.work.AskVerificationInputModel
import pt.isel.sitediary.domainmodel.work.Invite
import pt.isel.sitediary.domainmodel.work.Location
import pt.isel.sitediary.domainmodel.work.OpeningTerm
import pt.isel.sitediary.domainmodel.work.SiteDiaryLog
import pt.isel.sitediary.domainmodel.work.WorkInput
import pt.isel.sitediary.domainmodel.work.WorkState.FINISHED
import pt.isel.sitediary.domainmodel.work.WorkState.IN_PROGRESS
import pt.isel.sitediary.domainmodel.work.WorkState.VERIFYING
import pt.isel.sitediary.domainmodel.work.WorkType
import pt.isel.sitediary.model.EditWorkInputModel
import pt.isel.sitediary.model.FileModel
import pt.isel.sitediary.model.InviteInputModel
import pt.isel.sitediary.model.MemberInputModel
import pt.isel.sitediary.model.OpeningTermInputModel
import pt.isel.sitediary.repository.transaction.TransactionManager
import pt.isel.sitediary.utils.Errors
import pt.isel.sitediary.utils.Result
import pt.isel.sitediary.utils.failure
import pt.isel.sitediary.utils.success
import java.sql.Timestamp
import java.util.*

typealias CreateWorkResult = Result<Errors, Unit>

@Component
class WorkService(
    private val transactionManager: TransactionManager,
    private val clock: Clock
) {
    fun createWork(openingTerm: OpeningTermInputModel, user: User): CreateWorkResult = transactionManager.run {
        val workRep = it.workRepository
        val addressRep = it.addressRepository
        if (openingTerm.checkParams()) {
            failure(Errors.invalidParameter)
        } else if (openingTerm.checkTechnicians()) {
            failure(Errors.invalidTechnicians)
        } else {
            val location =
                addressRep.getLocation(
                    openingTerm.address.location.parish ?: "",
                    openingTerm.address.location.county ?: "",
                    openingTerm.address.location.district ?: ""
                )
            if (location == null) {
                failure(Errors.invalidLocation)
            } else {
                val work = WorkInput(
                    id = UUID.randomUUID(),
                    name = openingTerm.name,
                    description = openingTerm.description ?: "",
                    state = if (!openingTerm.verification.isNullOrBlank() && !openingTerm.checkCouncilWork(user)) VERIFYING else IN_PROGRESS,
                    type = WorkType.fromString(openingTerm.type) ?: WorkType.RESIDENCIAL,
                    address = Address(
                        Location(
                            location.district,
                            location.county,
                            location.parish
                        ),
                        openingTerm.address.street,
                        openingTerm.address.postalCode
                    ),
                    members = listOf(user.toMember()),
                    log = emptyList()
                )
                workRep.createWork(work, Timestamp.from(clock.now().toJavaInstant()), openingTerm, user)
                success(Unit)
            }
        }
    }

    fun getWork(id: UUID, user: User) = transactionManager.run {
        val work = it.workRepository.getById(id)
        if (work == null) {
            failure(Errors.workNotFound)
        } else if (user.role != "ADMIN" && !work.members.containsMemberById(user.id)) {
            failure(Errors.notMember)
        } else {
            val workResult = work.copy(log = work.log.map { log ->
                if (log.editable && log.checkIfEditTimeElapsed(clock)) {
                    it.logRepository.finish(log.id)
                    log.copy(editable = false)
                } else {
                    if (log.author.id != user.id) log.copy(editable = false)
                    else log
                }
            })
            success(workResult)
        }
    }

    fun getWorkList(user: User) = transactionManager.run {
        val work = when (user.role) {
            "ADMIN" -> it.workRepository.getWorkListAdmin()
            "CÂMARA" -> it.workRepository.getWorkListCouncil(user.location, user)
            else -> it.workRepository.getWorkList(user.id)
        }
        success(work)
    }

    fun getWorksPending(user: User) =
        transactionManager.run {
            val rep = it.workRepository
            when (user.role) {
                "ADMIN" -> {
                    val works = rep.getAllWorksPending()
                    success(works)
                }

                "CÂMARA" -> {
                    val works = rep.getWorksPending(user.location)
                    success(works)
                }

                else -> {
                    failure(Errors.forbidden)
                }
            }
        }

    fun answerPendingWork(workId: UUID, user: User, accepted: Boolean) = transactionManager.run {
        val rep = it.workRepository
        if (user.role != "ADMIN" && user.role != "CÂMARA") {
            failure(Errors.forbidden)
        } else if (accepted) {
            rep.acceptPending(workId, user.name, Timestamp.from(clock.now().toJavaInstant()))
            success(Unit)
        } else {
            rep.declinePending(workId, user.name, Timestamp.from(clock.now().toJavaInstant()))
            success(Unit)
        }
    }

    fun inviteMembers(members: List<MemberInputModel>, workId: UUID, userId: Int) = transactionManager.run {
        val userRep = it.usersRepository
        val workRep = it.workRepository
        val user = userRep.getUserById(userId)
        val work = workRep.getById(workId)
        if (user == null) {
            failure(Errors.userNotFound)
        } else if (work == null) {
            failure(Errors.workNotFound)
        } else if (!work.members.checkOwner(userId)) {
            failure(Errors.notAdmin)
        } else {
            members.forEach { m ->
                val id = userRep.getUserByEmail(m.email)?.id
                if (id != null) {
                    if (!work.members.containsMemberById(id) && !workRep.checkInvited(id, workId)) {
                        workRep.inviteMember(id, m.role, workId)
                    }
                } else {
                    val dummyId = userRep.createDummyUser(m.email)
                    workRep.inviteMember(dummyId, m.role, workId)
                }
                work.sendEmailInvitation(Invite(UUID.randomUUID(), m.email, m.role, workId))
            }
            success(Unit)
        }
    }


    fun getInviteList(userId: Int) = transactionManager.run {
        val user = it.usersRepository.getUserById(userId)
        if (user == null) {
            failure(Errors.userNotFound)
        } else {
            val invites = it.workRepository.getInviteList(userId)
            success(invites)
        }
    }

    fun getInvite(workId: UUID, userId: Int) = transactionManager.run {
        val user = it.usersRepository.getUserById(userId)
        if (user == null) {
            failure(Errors.userNotFound)
        } else {
            val invite = it.workRepository.getInvite(workId, userId)
            if (invite == null) {
                failure(Errors.inviteNotFound)
            } else {
                success(invite)
            }
        }
    }

    fun answerInvite(inviteInput: InviteInputModel, user: User) = transactionManager.run {
        val workRep = it.workRepository
        val invite = workRep.getInvite(inviteInput.workId, user.id)
        if (invite == null) {
            failure(Errors.inviteNotFound)
        } else {
            if (inviteInput.accepted) {
                workRep.acceptInvite(invite, user)
                success(Unit)
            } else {
                workRep.declineInvite(inviteInput.workId, user.id)
                success(Unit)
            }
        }
    }

    fun finishWork(workId: UUID, userId: Int) = transactionManager.run {
        val workRep = it.workRepository
        val work = workRep.getById(workId)
        if (work == null) {
            failure(Errors.workNotFound)
        } else if (!work.members.checkOwner(userId)) {
            failure(Errors.notAdmin)
        } else if (work.state != IN_PROGRESS) {
            failure(Errors.workAlreadyFinished)
        } else {
            val requiredTechnicians = workRep.checkRequiredTechnicians(workId)
            if (!requiredTechnicians) {
                failure(Errors.membersMissing)
            } else {
                workRep.finishWork(workId)
                success(Unit)
            }
        }
    }

    fun getWorkImage(workId: UUID, user: User) = transactionManager.run {
        val workRep = it.workRepository
        val work = workRep.getById(workId)
        if (work == null) {
            failure(Errors.workNotFound)
        } else if (user.role != "ADMIN" && !work.members.containsMemberById(user.id)) {
            failure(Errors.notMember)
        } else {
            val file = workRep.getWorkImage(workId)
            success(file)
        }
    }

    fun changeWorkImage(workId: UUID, featuredImage: FileModel?, userId: Int) = transactionManager.run {
        val workRep = it.workRepository
        val work = workRep.getById(workId)
        if (work == null) {
            failure(Errors.workNotFound)
        } else if (!work.members.checkOwner(userId)) {
            failure(Errors.notMember)
        } else {
            if (workRep.checkWorkImageExists(workId) == null && featuredImage != null) {
                workRep.insertWorkImage(workId, featuredImage)
            } else {
                if (featuredImage != null) workRep.changeWorkImage(workId, featuredImage) else workRep.removeWorkImage(
                    workId
                )
            }
            success(Unit)
        }
    }

    fun getNumberOfInvites(id: Int) = transactionManager.run {
        success(it.workRepository.getNumberOfInvites(id))
    }

    fun getMemberProfile(workId: UUID, member: String, user: User) =
        transactionManager.run {
            val rep = it.workRepository
            val work = rep.getById(workId)
            if (work == null) {
                failure(Errors.workNotFound)
            } else {
                val profile = rep.getMemberProfile(workId.toString(), member)
                if (profile == null) {
                    failure(Errors.memberNotFound)
                } else {
                    success(profile)
                }
            }
        }

    fun editWork(workId: UUID, editWork: EditWorkInputModel, user: User) =
        transactionManager.run {
            val workRep = it.workRepository
            val addressRep = it.addressRepository
            val work = workRep.getById(workId)
            if (work == null) {
                failure(Errors.workNotFound)
            } else if (!work.members.checkOwner(user.id)) {
                failure(Errors.notAdmin)
            } else {
                if (work.state == FINISHED) {
                    failure(Errors.workAlreadyFinished)
                } else {
                    val location = addressRep.getLocation(
                        editWork.address.location.parish ?: "",
                        editWork.address.location.county ?: "",
                        editWork.address.location.district ?: ""
                    )
                    if (location == null) {
                        failure(Errors.invalidLocation)
                    } else {
                        workRep.editWork(workId, editWork)
                        success(Unit)
                    }
                }
            }
        }

    fun askWorkVerification(verification: AskVerificationInputModel, user: User) = transactionManager.run {
        val work = it.workRepository.getById(verification.workId)
        if (verification.verificationDoc.isBlank()) {
            failure(Errors.invalidVerificationDoc)
        } else if (work == null) {
            failure(Errors.workNotFound)
        } else if (!work.members.checkOwner(user.id)) {
            failure(Errors.notAdmin)
        } else {
            it.workRepository.askWorkVerification(verification.workId, verification.verificationDoc)
            success(Unit)
        }
    }

    fun getSiteDiary(workId: UUID, user: User) = transactionManager.run {
        val workRep = it.workRepository
        val work = workRep.getById(workId)
        if (work == null) {
            failure(Errors.workNotFound)
        } else if (user.role != "ADMIN" && !work.members.containsMemberById(user.id)) {
            failure(Errors.notMember)
        } else {
            val siteDiary = workRep.getSiteDiary(workId)
            val htmlBuilder = StringBuilder()
            appendOpeningTerm(siteDiary.toOpeningTerm(), htmlBuilder)
            appendLogs(siteDiary.logs, htmlBuilder)
            htmlBuilder.append("</div>\n</body>\n</html>")
            success(htmlBuilder.toString().toByteArray())
        }
    }

    fun getOpeningTerm(workId: UUID, user: User) = transactionManager.run {
        val workRep = it.workRepository
        val work = workRep.getById(workId)
        if (work == null) {
            failure(Errors.workNotFound)
        } else if (user.role != "ADMIN" && !(user.role == "CÂMARA" && work.checkCouncil(user)) &&
            !(user.role != "CÂMARA" && work.members.containsMemberById(user.id))) {
            failure(Errors.notMember)
        } else {
            val openingTerm = workRep.getOpeningTerm(workId)
            val htmlBuilder = StringBuilder()
            appendOpeningTerm(openingTerm, htmlBuilder)
            htmlBuilder.append("</div>\n</body>\n</html>")
            success(htmlBuilder.toString().toByteArray())
        }
    }
}

private fun appendOpeningTerm(openingTerm: OpeningTerm, htmlBuilder: StringBuilder) {
    htmlBuilder.append(html)
    htmlBuilder.append(
        "<p>A presente obra dispõe de verificação com o documento (1)<span class=\"input-camp\">" +
                "${openingTerm.verification.doc}</span></p>\n<p>Câmara Municipal de <span " +
                "class=\"input-camp\">${openingTerm.location.county}</span></p>\n<p>Identificações e " +
                "indicações obrigatórias de acordo com o previsto no ponto 4 da Portaria nº " +
                "1268/2008:</p>\n</div>\n"
    )
    htmlBuilder.append(
        "<div class=\"form-section\">\n<p>a) TITULAR DA LICENÇA/DO TÍTULO (2) <span class=\"input-camp\"> " +
                "${openingTerm.licenseHolder}</span></p>\n<p>b) TÉCNICO RESPONSÁVEL PELA DIREÇÃO DE " +
                "FISCALIZAÇÃO DA OBRA <span class=\"input-camp\">" +
                "${openingTerm.authors["fiscalization"]?.name ?: NO_INPUT}</span> inscrito na (3) " +
                "<span class=\"input-camp\">" +
                "${openingTerm.authors["fiscalization"]?.association ?: NO_INPUT}</span>, com o nº " +
                "<span class=\"input-camp\">" +
                "${openingTerm.authors["fiscalization"]?.num ?: NO_INPUT}</span></p>\n" +
                "<p>c) COORDENADOR DE PROJETO <span class=\"input-camp\">" +
                "${openingTerm.authors["coordinator"]?.name ?: NO_INPUT}</span> inscrito na (3) <span " +
                "class=\"input-camp\">${openingTerm.authors["coordinator"]?.association ?: NO_INPUT}</span>, " +
                "com o nº <span class=\"input-camp\">${openingTerm.authors["coordinator"]?.num}</span>" +
                "</p>\n</div>\n"
    )
    htmlBuilder.append(
        "<div class=\"form-section\">\n<p>AUTORES DOS PROJETOS:</p>\n<p>Arquitetura <span class=\"input-camp\">" +
                "${openingTerm.authors["architect"]?.name ?: NO_INPUT}</span> inscrito na (3) <span " +
                "class=\"input-camp\">${openingTerm.authors["architect"]?.association ?: NO_INPUT}</span>, " +
                "com o nº <span class=\"input-camp\">${openingTerm.authors["architect"]?.num ?: NO_INPUT}</span>\n" +
                "</p>\n<p>Estabilidade <span class=\"input-camp\">" +
                "${openingTerm.authors["stability"]?.name ?: NO_INPUT}</span> inscrito na (3) <span " +
                "class=\"input-camp\">${openingTerm.authors["stability"]?.association ?: NO_INPUT}</span>, " +
                "com o nº <span class=\"input-camp\">${openingTerm.authors["stability"]?.num ?: NO_INPUT}</span>\n" +
                "</p>\n<p>Alimentação e distribuição de energia elétrica <span class=\"input-camp\">" +
                "${openingTerm.authors["electricity"]?.name ?: NO_INPUT}</span> inscrito na (3) <span " +
                "class=\"input-camp\">${openingTerm.authors["electricity"]?.association ?: NO_INPUT}</span>, " +
                "com o nº <span class=\"input-camp\">${openingTerm.authors["electricity"]?.num ?: NO_INPUT}</span>\n" +
                "</p>\n<p>Instalação de gás <span class=\"input-camp\">" +
                "${openingTerm.authors["gas"]?.name ?: NO_INPUT}</span> inscrito na (3) <span class=\"input-camp\">" +
                "${openingTerm.authors["gas"]?.association ?: NO_INPUT}</span>, com o nº <span class=\"input-camp\">" +
                "${openingTerm.authors["gas"]?.num ?: NO_INPUT}</span>\n</p>\n<p>Águas e esgotos <span " +
                "class=\"input-camp\">${openingTerm.authors["water"]?.name ?: NO_INPUT}</span> inscrito na (3) <span " +
                "class=\"input-camp\">${openingTerm.authors["water"]?.association ?: NO_INPUT}</span>, com o nº " +
                "<span class=\"input-camp\">${openingTerm.authors["water"]?.num ?: NO_INPUT}</span>\n</p>\n" +
                "<p>Instalações telefónicas e de telecomunicações <span class=\"input-camp\">" +
                "${openingTerm.authors["phone"]?.name ?: NO_INPUT}</span>inscrito na (3) <span class=\"input-camp\">" +
                "${openingTerm.authors["phone"]?.association ?: NO_INPUT}</span>, com o nº <span class=\"input-camp\"" +
                ">${openingTerm.authors["phone"]?.num ?: NO_INPUT}</span>\n</p>\n<p>Comportamento térmico <span " +
                "class=\"input-camp\">${openingTerm.authors["isolation"]?.name ?: NO_INPUT}</span> inscrito na (3) " +
                "<span class=\"input-camp\">${openingTerm.authors["isolation"]?.association ?: NO_INPUT}</span>, " +
                "com o nº <span class=\"input-camp\">${openingTerm.authors["isolation"]?.num ?: NO_INPUT}</span>\n" +
                "</p>\n<p>Condicionamento acústico <span class=\"input-camp\">" +
                "${openingTerm.authors["acoustic"]?.name ?: NO_INPUT}</span> inscrito na (3) <span " +
                "class=\"input-camp\">${openingTerm.authors["acoustic"]?.association ?: NO_INPUT}</span>, com o nº " +
                "<span class=\"input-camp\">${openingTerm.authors["acoustic"]?.num ?: NO_INPUT}</span>\n</p>\n" +
                "<p>Instalações eletromecânicas de transporte de pessoas e/ou mercadorias <span class=\"input-camp\">" +
                "${openingTerm.authors["transport"]?.name ?: NO_INPUT}</span> inscrito na (3) <span " +
                "class=\"input-camp\">${openingTerm.authors["transport"]?.association ?: NO_INPUT}</span>, com o nº " +
                "<span class=\"input-camp\">${openingTerm.authors["transport"]?.num ?: NO_INPUT}</span>\n</p>\n</div>\n"
    )
    htmlBuilder.append(
        "<div class=\"form-section\">\n<p>d) EMPRESA DE CONSTRUÇÃO <span class=\"input-camp\">" +
                "${openingTerm.company.name}</span> Nº do alvará (4) <span class=\"input-camp\">" +
                "${openingTerm.company.num}</span></p>\n<p>e) DIRETOR DA OBRA (5) <span class=\"input-camp\">" +
                "${openingTerm.authors["director"]?.name ?: NO_INPUT}</span> inscrito na (3) <span " +
                "class=\"input-camp\">${openingTerm.authors["director"]?.association ?: NO_INPUT}</span>, com o nº " +
                "<span class=\"input-camp\">${openingTerm.authors["director"]?.num ?: NO_INPUT}</span>\n</p>\n" +
                "<p>f) TIPO DE OBRA A EXECUTAR (6) <span class=\"input-camp\">${openingTerm.type}</span></p>\n" +
                "<p>g) IDENTIFICAÇÃO DO PRÉDIO</p>\n<p>sito em <span class=\"input-camp\">" +
                "${openingTerm.location.street}</span> da freguesia de <span class=\"input-camp\">" +
                "${openingTerm.location.parish}, ${openingTerm.location.postalCode} </span> com a identificação " +
                "<span class=\"input-camp\">${openingTerm.location.building}</span>\n</p>\n</div>\n"
    )
    htmlBuilder.append(
        "<div class=\"form-section\"><p>Data <span class=\"input-camp\">${openingTerm.verification.dt_signature}" +
                "</span></p><p>Assinatura (7) <span class=\"input-camp\">${openingTerm.verification.signature}</span>" +
                "</p>\n</div>\n"
    )
}

private fun appendLogs(logs: List<SiteDiaryLog>, htmlBuilder: StringBuilder) {
    htmlBuilder.append("<div class=\"form-section\">\n<h2>LIVRO DE OBRA</h2>\n")
    htmlBuilder.append("<h3>Registos</h3>\n")
    logs.forEach { log ->
        htmlBuilder.append("<p>Autor: ${log.author}</p>\n")
        htmlBuilder.append("<p>Data de Criação: ${log.createdAt}</p>\n")
        htmlBuilder.append("<p>Data de Alteração: ${log.lastModificationAt}</p>\n")
        htmlBuilder.append("<p>Observação: ${log.content}</p>\n")
        htmlBuilder.append("<hr></hr>\n")
    }
    htmlBuilder.append("</div>\n")
}

const val NO_INPUT = "______________"
const val html = "<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n<meta charset=\"UTF-8\" />\n<meta name=\"viewport\" " +
        "content=\"width=device-width, initial-scale=1.0\" />\n<title>Termo de Abertura</title>\n<style>\nbody {\n" +
        "font-family: Arial, sans-serif;\njustify-content: center;\n}\np {\nmargin-bottom: 5px;\n}\n.container {\n" +
        "width: 70%;\nmargin: 0 auto;\n}\n.form-section {\nmargin-bottom: 20px;\n}\n.form-section h2 {\n" +
        "text-align: center;\n}\n.form-section p {\nmargin: 5px 0;\n}\n.form-section input {\nwidth: 100%;\n" +
        "padding: 8px;\nmargin: 5px 0;\nbox-sizing: border-box;\n}\n.signature input {\nwidth: auto;\n}\n" +
        ".input-camp {\ncolor: blue;\n}\n</style>\n</head>\n<body>\n<div class=\"container\">\n<div " +
        "class=\"form-section\">\n<h2>TERMO DE ABERTURA</h2>\n<p>Nos termos do artigo 97º do Decreto-Lei nº 555/99, " +
        "de 16 de dezembro, com a redação dada pelo Decreto-Lei nº 26/2010, de 30 de março e da Portaria nº " +
        "1268/2008, de 6 de novembro, é lavrado o presente termo de abertura do livro de obra para nele serem " +
        "exarados, na data da sua ocorrência, todos os factos e observações relativos à execução da obra, bem como " +
        "o registo periódico do seu estado de execução.</p>\n"
