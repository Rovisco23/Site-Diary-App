package pt.isel.sitediary.repository.jdbi

import org.jdbi.v3.core.Handle
import pt.isel.sitediary.domainmodel.user.Technician
import pt.isel.sitediary.domainmodel.user.User
import pt.isel.sitediary.domainmodel.work.*
import pt.isel.sitediary.model.EditWorkInputModel
import pt.isel.sitediary.model.FileModel
import pt.isel.sitediary.model.OpeningTermInputModel
import pt.isel.sitediary.repository.WorkRepository
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.*

class JdbiWork(private val handle: Handle) : WorkRepository {
    override fun createWork(work: WorkInput, createdAt: Timestamp, openingTerm: OpeningTermInputModel, user: User) {
        handle.createUpdate(
            "insert into OBRA(id, nome, tipo, descricao, estado, freguesia, concelho, distrito, rua, cpostal)" +
                    "values (:id, :nome, :tipo, :descricao, :estado, :freguesia, :concelho, :distrito, :rua, :cpostal)"
        )
            .bind("id", work.id)
            .bind("nome", work.name)
            .bind("tipo", work.type.toString())
            .bind("descricao", work.description)
            .bind("estado", work.state.toString())
            .bind("freguesia", work.address.location.parish)
            .bind("concelho", work.address.location.county)
            .bind("distrito", work.address.location.district)
            .bind("rua", work.address.street)
            .bind("cpostal", work.address.postalCode)
            .execute()
        handle.createUpdate(
            "insert into MEMBRO(oId, uId, role) values (:id_obra, :id_utilizador, :role)"
        )
            .bind("id_obra", work.id)
            .bind("id_utilizador", user.id)
            .bind("role", "DONO")
            .execute()
        addCouncilAsMember(work.id, work.address.location)
        val companyId = getCompanyId(openingTerm.company.name, openingTerm.company.num)
        val councilId = getCouncil(work.address.location)
        val tId = insertOpeningTerm(openingTerm, createdAt, companyId, work.id, councilId, user)
        insertTechnicians(openingTerm.technicians, tId, work.id)
    }

    override fun getById(id: UUID): Work? = handle.createQuery(
        "SELECT OBRA.id, OBRA.nome, OBRA.tipo, OBRA.descricao, OBRA.estado, OBRA.distrito, OBRA.concelho, " +
                "OBRA.freguesia, OBRA.rua, OBRA.cpostal, ARRAY(SELECT CONCAT(uId, ';', username, ';', MEMBRO.role) " +
                "FROM MEMBRO JOIN UTILIZADOR ON uId = id WHERE oId = :id AND MEMBRO.pendente = 'false') AS membros, " +
                "ARRAY(SELECT CONCAT(nome, ';', email, ';', role, ';', associacao, ';', numero) FROM INTERVENIENTE WHERE " +
                "oId = :id) AS technicians, ARRAY(SELECT CONCAT(REGISTO.id, ';', author, ';', UTILIZADOR.username, " +
                "';', (SELECT Membro.role from Membro join Registo r on r.oId = Membro.oId where Membro.uid = author " +
                "and r.id = REGISTO.id), ';', editable, ';', COUNT(i.name) > 0 OR COUNT(d.name) > 0, ';', " +
                "REGISTO.creation_date) FROM REGISTO LEFT JOIN IMAGEM i ON i.rId = REGISTO.id LEFT JOIN DOCUMENTO d " +
                "ON d.rId = REGISTO.id JOIN UTILIZADOR ON author = UTILIZADOR.id JOIN MEMBRO ON uId = author WHERE " +
                "REGISTO.oId = :id GROUP BY REGISTO.id, author, UTILIZADOR.username, (SELECT Membro.role from Membro " +
                "join Registo r on r.oId = Membro.oId where Membro.uid = author and r.id = REGISTO.id), editable, " +
                "REGISTO.creation_date ORDER BY REGISTO.creation_date) AS log, TERMO_ABERTURA.titular_licenca, TERMO_ABERTURA.autorizacao, TERMO_ABERTURA.predio, " +
                "EMPRESA_CONSTRUCAO.nome AS company_name, EMPRESA_CONSTRUCAO.numero AS company_num, (SELECT COUNT(*) " +
                "FROM IMAGEM WHERE oId = OBRA.id) AS imagens, (SELECT COUNT(*) FROM DOCUMENTO WHERE oId = OBRA.id) " +
                "AS documentos, (TERMO_ABERTURA.assinatura IS NOT NULL and Obra.estado != 'REJEITADA') AS verification FROM OBRA JOIN TERMO_ABERTURA " +
                "ON TERMO_ABERTURA.oId = OBRA.id JOIN EMPRESA_CONSTRUCAO ON EMPRESA_CONSTRUCAO.id = " +
                "TERMO_ABERTURA.empresa_construcao WHERE OBRA.id = :id"
    )
        .bind("id", id.toString())
        .mapTo(Work::class.java)
        .singleOrNull()

    override fun getWorkList(userId: Int): List<WorkSimplified> = handle.createQuery(
        "select OBRA.id, OBRA.nome, ta.titular_licenca as owner, Obra.tipo, OBRA.descricao, OBRA.estado," +
                " OBRA.freguesia, OBRA.concelho, OBRA.distrito, OBRA.rua, OBRA.cpostal, (ta.assinatura IS NOT NULL and " +
                "OBRA.estado != 'REJEITADA') AS verification from MEMBRO join OBRA on id = oId join TERMO_ABERTURA ta " +
                "on OBRA.id = ta.oId where uId = :id and MEMBRO.pendente = 'false'"
    )
        .bind("id", userId)
        .mapTo(WorkSimplified::class.java)
        .list()

    override fun getOpeningTerm(workId: UUID): OpeningTerm = handle.createQuery(
        "select autorizacao, assinatura, dt_assinatura, o.concelho, o.freguesia, o.rua, o.cpostal, predio, " +
                "ec.nome as nome_empresa, ec.numero as numero_empresa, titular_licenca, o.tipo, " +
                "ARRAY(SELECT CONCAT(role, ';', nome, ';', associacao, ';', numero) FROM INTERVENIENTE " +
                "WHERE oId = :id) AS technicians from TERMO_ABERTURA ta join Obra o on o.id = ta.oId join " +
                "empresa_construcao ec on ec.id = empresa_construcao where ta.oId = :id"
    )
        .bind("id", workId.toString())
        .mapTo(OpeningTerm::class.java)
        .single()

    private fun getCouncil(location: Location) =
        handle.createQuery(
            "select id from Localidade where freguesia = :freguesia and concelho = " +
                    ":concelho and distrito = :distrito"
        )
            .bind("freguesia", location.parish)
            .bind("concelho", location.county)
            .bind("distrito", location.district)
            .mapTo(Int::class.java)
            .single()

    private fun getCompanyId(name: String, number: Int) = handle.createQuery(
        "select id from EMPRESA_CONSTRUCAO where nome = :nome and numero = :numero"
    )
        .bind("nome", name)
        .bind("numero", number)
        .mapTo(Int::class.java)
        .singleOrNull() ?: handle.createUpdate(
        "insert into EMPRESA_CONSTRUCAO(nome, numero)" +
                "values (:nome, :numero)"
    )
        .bind("nome", name)
        .bind("numero", number)
        .executeAndReturnGeneratedKeys()
        .mapTo(Int::class.java)
        .one()

    private fun insertOpeningTerm(
        openingTerm: OpeningTermInputModel,
        createdAt: Timestamp,
        companyId: Int,
        workId: UUID,
        councilId: Int,
        user: User
    ): Int {
        val assinatura =
            if (openingTerm.checkCouncilWork(user) && !openingTerm.verification.isNullOrBlank()) user.name else null
        val time = if (assinatura != null) createdAt else null
        return handle.createUpdate(
            "insert into TERMO_ABERTURA(oId, inicio, camara, titular_licenca, empresa_construcao, autorizacao, assinatura, dt_assinatura, predio)" +
                    "values (:oId, :inicio, :camara, :titular_licença, :empresa_construção, :doc, :assinatura, :createdAt, :predio)"
        )
            .bind("oId", workId)
            .bind("inicio", Timestamp.valueOf(LocalDateTime.now()))
            .bind("camara", councilId)
            .bind("titular_licença", openingTerm.holder)
            .bind("doc", if (!openingTerm.verification.isNullOrBlank()) openingTerm.verification else null)
            .bind("empresa_construção", companyId)
            .bind("predio", openingTerm.building)
            .bind("assinatura", assinatura)
            .bind("createdAt", time)
            .executeAndReturnGeneratedKeys()
            .mapTo(Int::class.java)
            .one()
    }

    override fun inviteMembers(invites: List<Invite>) {
        val query = StringBuilder("insert into CONVITE(id, email, role, oId) values ")
        invites.forEach {
            query.append("('${it.id}', '${it.email}', '${it.role}', '${it.workId}'), ")
        }
        handle.createUpdate(query.toString().dropLast(2)).execute()
    }

    override fun checkInvited(id: Int, workId: UUID): Boolean = handle.createQuery(
        "select count(*) from MEMBRO where uId = :uId and oId = :oId and pendente = 'true'"
    )
        .bind("uId", id)
        .bind("oId", workId.toString())
        .mapTo(Int::class.java)
        .single() > 0

    override fun getInviteList(userId: Int): List<InviteSimplified> = handle.createQuery(
        "with Table1 as (select Obra.id, Obra.nome as workTitle, Membro.role from Obra join Membro on Obra.id = " +
                "Membro.oId where Membro.uId = :uId and pendente = :pending), Table2 as (select Obra.id, " +
                "Utilizador.username as owner from Membro join Utilizador on Membro.uId = Utilizador.id join Obra " +
                "on Membro.oId = Obra.id where Membro.role = 'DONO' and Membro.uId != :uId and Membro.oId = Obra.id) " +
                "select Table1.id, Table1.workTitle, Table1.role, Table2.owner from Table1 inner join Table2 on " +
                "Table1.id = Table2.id;"
    )
        .bind("uId", userId)
        .bind("pending", true)
        .mapTo(InviteSimplified::class.java)
        .list()

    override fun getInvite(workId: UUID, userId: Int): InviteSimplified? = handle.createQuery(
        "select Obra.id, Obra.nome as workTitle, Membro.role, (select Utilizador.username from Membro join " +
                "Utilizador on Membro.uId = Utilizador.id where Membro.oId = :oId and Membro.role = 'DONO') " +
                "as owner from Obra join Membro on Obra.id = Membro.oId where uid = :uId and Membro.pendente = 'true' " +
                "and Membro.oId = :oId;"
    )
        .bind("oId", workId.toString())
        .bind("uId", userId)
        .mapTo(InviteSimplified::class.java)
        .singleOrNull()

    override fun acceptInvite(inv: InviteSimplified, user: User) {
        handle.createUpdate(
            "update MEMBRO set pendente = :pendente where uId = :uId and oId = :oId"
        )
            .bind("uId", user.id)
            .bind("oId", inv.workId.toString())
            .bind("pendente", false)
            .execute()

        if (inv.checkTechnician()) {
            val tId = handle.createQuery(
                "select id from TERMO_ABERTURA where oId = :oId"
            )
                .bind("oId", inv.workId.toString())
                .mapTo(Int::class.java)
                .single()
            val roleDoesntExists = handle.createQuery(
                "select count(*)=0 from Interveniente where role=:role AND tid = :tId"
            )
                .bind("role", inv.role)
                .bind("tId", tId)
                .mapTo(Boolean::class.java)
                .single()
            if (roleDoesntExists) {
                val name = handle.createQuery(
                    "select CONCAT(nome,' ',apelido) as nome from UTILIZADOR where id = :uId"
                )
                    .bind("uId", user.id)
                    .mapTo(String::class.java)
                    .single()

                // Adicionar ao Interveniente
                handle.createUpdate(
                    "insert into INTERVENIENTE(tId, oId, nome, email, role, associacao, numero) values(:tId, :oId, :nome, :email, " +
                            ":role, :association,:num)"
                )
                    .bind("tId", tId)
                    .bind("oId", inv.workId.toString())
                    .bind("email", user.email)
                    .bind("nome", name)
                    .bind("role", inv.role)
                    .bind("association", user.association.name)
                    .bind("num", user.association.number)
                    .execute()
            } else {
                handle.createUpdate(
                    "update INTERVENIENTE set nome = :nome, email = :email, associacao = :association, numero = :num where tId = :tId and oId = :oId and role = :role"
                )
                    .bind("oId", inv.workId.toString())
                    .bind("tId", tId)
                    .bind("email", user.email)
                    .bind("nome", user.name)
                    .bind("role", inv.role)
                    .bind("association", user.association.name)
                    .bind("num", user.association.number)
                    .execute()
            }
        }
    }

    override fun declineInvite(workId: UUID, userId: Int) {
        handle.createUpdate("delete from MEMBRO where uId = :uId and oId = :oId")
            .bind("uId", userId)
            .bind("oId", workId.toString())
            .execute()
    }

    override fun getWorkListAdmin(): List<WorkSimplified> = handle.createQuery(
        "select o.id, o.nome, ta.titular_licenca as owner,  o.tipo, o.descricao, o.estado, o.freguesia," +
                " o.concelho, o.distrito, o.rua, o.cpostal, (ta.assinatura IS NOT NULL and " +
                "o.estado != 'REJEITADA') AS verification from OBRA o join TERMO_ABERTURA ta on ta.oId = o.id"
    )
        .mapTo(WorkSimplified::class.java)
        .list()

    override fun getWorkListCouncil(location: Location, user: User): List<WorkSimplified> = handle.createQuery(
        "select distinct o.id, o.nome, ta.titular_licenca as owner, o.tipo, o.descricao, o.estado, o.freguesia, o.concelho," +
                " o.distrito, o.rua, o.cpostal, (ta.assinatura IS NOT NULL and o.estado != 'REJEITADA') AS verification " +
                "from OBRA o join TERMO_ABERTURA ta on ta.oId = o.id join MEMBRO m on ta.oid = m.oid " +
                "where (freguesia = :parish and concelho = :county and distrito = :district) or (m.oId = o.id and m.uid = :id)"
    )
        .bind("id", user.id)
        .bind("parish", location.parish)
        .bind("county", location.county)
        .bind("district", location.district)
        .mapTo(WorkSimplified::class.java)
        .list()

    override fun getWorkImage(workId: UUID) = handle.createQuery(
        "select file, name, type from IMAGEM_OBRA where work_id = :id"
    )
        .bind("id", workId.toString())
        .mapTo(FileModel::class.java)
        .singleOrNull()

    override fun checkWorkImageExists(workId: UUID) = handle.createQuery(
        "select work_id from IMAGEM_OBRA where work_id = :id"
    )
        .bind("id", workId.toString())
        .mapTo(UUID::class.java)
        .singleOrNull()

    override fun insertWorkImage(workId: UUID, featuredImage: FileModel) {
        handle.createUpdate("insert into IMAGEM_OBRA(work_id, name, type, file) values (:id, :name, :type, :img)")
            .bind("id", workId.toString())
            .bind("name", featuredImage.fileName)
            .bind("type", featuredImage.contentType)
            .bind("img", featuredImage.file)
            .execute()
    }

    override fun changeWorkImage(workId: UUID, featuredImage: FileModel) {
        handle.createUpdate("update IMAGEM_OBRA set name = :name, type = :type, file = :img where work_id = :id")
            .bind("id", workId.toString())
            .bind("name", featuredImage.fileName)
            .bind("type", featuredImage.contentType)
            .bind("img", featuredImage.file)
            .execute()
    }

    override fun removeWorkImage(workId: UUID) {
        handle.createUpdate("delete from IMAGEM_OBRA where work_id = :id")
            .bind("id", workId.toString())
            .execute()
    }

    override fun finishWork(workId: UUID) {
        handle.createUpdate("update OBRA set estado = :state, data_conclusao = :date where id = :id")
            .bind("id", workId.toString())
            .bind("state", WorkState.FINISHED.toString())
            .bind("date", Timestamp.valueOf(LocalDateTime.now()))
            .execute()
    }

    override fun inviteMember(id: Int, role: String, workId: UUID) {
        handle.createUpdate("insert into MEMBRO(uId, oId, role, pendente) values(:uId, :oId, :role, :pendente)")
            .bind("uId", id)
            .bind("oId", workId.toString())
            .bind("role", role)
            .bind("pendente", true)
            .execute()
    }

    override fun checkRequiredTechnicians(workId: UUID): Boolean = handle.createQuery(
        "select count(*) from INTERVENIENTE where (oId = :id and (role = 'FISCALIZAÇÃO' or role = 'COORDENADOR'))"
    )
        .bind("id", workId.toString())
        .mapTo(Int::class.java)
        .single() == 2

    override fun getNumberOfInvites(id: Int): Int = handle.createQuery(
        "select count(*) from Membro where uId = :uId and pendente = 'True'"
    )
        .bind("uId", id)
        .mapTo(Int::class.java)
        .single()

    override fun getAllWorksPending(): List<WorkVerifying> = handle.createQuery(
        "select o.id, o.nome, u.nome as ownerFirstName, u.apelido as ownerLastName, o.tipo, o.descricao, o.estado," +
                " o.freguesia, o.concelho, o.distrito, o.rua, o.cpostal from OBRA o " +
                "join TERMO_ABERTURA ta on o.id = ta.oid join MEMBRO m on ta.oId = m.oId join" +
                " UTILIZADOR u on m.uId = u.id where o.estado = 'EM VERIFICAÇÃO' and m.role = 'DONO'"
    )
        .mapTo(WorkVerifying::class.java)
        .list()

    override fun getWorksPending(location: Location): List<WorkVerifying> = handle.createQuery(
        "select o.id, o.nome, u.nome as ownerFirstName, u.apelido as ownerLastName, o.tipo, o.descricao, o.estado," +
                " o.freguesia, o.concelho, o.distrito, o.rua, o.cpostal from OBRA o " +
                "join TERMO_ABERTURA ta on o.id = ta.oid join MEMBRO m on ta.oId = m.oId " +
                "join UTILIZADOR u on m.uId = u.id where o.estado = 'EM VERIFICAÇÃO' and m.role = 'DONO' " +
                "and o.concelho = :county and o.distrito = :district"
    )
        .bind("county", location.county)
        .bind("district", location.district)
        .mapTo(WorkVerifying::class.java)
        .list()

    override fun acceptPending(workId: UUID, user: String, dateAuth: Timestamp) {
        handle.createUpdate(
            "update OBRA set estado = :state where id = :oId;" +
                    "update termo_abertura set dt_assinatura = :date, assinatura = :user where oId = :oId"
        )
            .bind("oId", workId.toString())
            .bind("state", WorkState.IN_PROGRESS.toString())
            .bind("date", dateAuth)
            .bind("user", user)
            .execute()
    }

    override fun declinePending(workId: UUID, user: String, dateAuth: Timestamp) {
        handle.createUpdate(
            "update OBRA set estado = :state where id = :oId;" +
                    "update termo_abertura set dt_assinatura = :date, assinatura = :user where oId = :oId"
        )
            .bind("oId", workId.toString())
            .bind("state", WorkState.REJECTED.toString())
            .bind("date", dateAuth)
            .bind("user", user)
            .execute()
    }

    override fun getMemberProfile(workId: String, member: String): MemberProfile? = handle.createQuery(
        "select u.id, u.nome, u.apelido, u.email, m.role, u.telefone, u.freguesia, u.concelho, u.distrito " +
                "from MEMBRO m join UTILIZADOR u on m.uId = u.id where m.oId = :workId and u.username = :username"
    )
        .bind("workId", workId)
        .bind("username", member)
        .mapTo(MemberProfile::class.java)
        .singleOrNull()

    override fun editWork(workId: UUID, editWork: EditWorkInputModel) {
        handle.createUpdate(
            "update OBRA set nome = :nome, descricao = :descricao, tipo = :tipo, freguesia = :parish, concelho = :county, " +
                    "distrito = :district, rua = :street, cpostal = :postalCode where id = :id"
        )
            .bind("id", workId.toString())
            .bind("nome", editWork.name)
            .bind("descricao", editWork.description)
            .bind("tipo", editWork.type)
            .bind("parish", editWork.address.location.parish)
            .bind("county", editWork.address.location.county)
            .bind("district", editWork.address.location.district)
            .bind("street", editWork.address.street)
            .bind("postalCode", editWork.address.postalCode)
            .execute()

        handle.createUpdate(
            "delete from INTERVENIENTE where oId = :id"
        )
            .bind("id", workId.toString())
            .execute()
        val tId = handle.createQuery(
            "select id from TERMO_ABERTURA where oId = :id"
        )
            .bind("id", workId.toString())
            .mapTo(Int::class.java)
            .single()
        val cId = getCompanyId(editWork.company.name, editWork.company.num)
        handle.createUpdate(
            "update TERMO_ABERTURA set titular_licenca = :titular, empresa_construcao = :empresa, predio = :predio where oId = :id"
        )
            .bind("titular", editWork.licenseHolder)
            .bind("empresa", cId)
            .bind("predio", editWork.building)
            .bind("id", workId.toString())
            .execute()
        insertTechnicians(editWork.technicians, tId, workId)
    }

    override fun askWorkVerification(id: UUID, doc: String) {
        handle.createUpdate(
            "update TERMO_ABERTURA set autorizacao = :doc, assinatura = null, dt_assinatura = null where oId = :id"
        )
            .bind("doc", doc)
            .bind("id", id.toString())
            .execute(
        )
        handle.createUpdate(
            "update OBRA set estado = :state where id = :id"
        )
            .bind("state", WorkState.VERIFYING.toString())
            .bind("id", id.toString())
            .execute(
        )
    }

    override fun deleteWork(id: Int) {
        val workId = handle.createQuery(
                "select oId from MEMBRO where uId = :id"
        )
            .bind("id", id)
            .mapTo(UUID::class.java)
            .single()
        val constructionCompanyId = handle.createQuery(
            "select empresa_construcao from TERMO_ABERTURA where oId = :id"
        )
            .bind("id", workId.toString())
            .mapTo(Int::class.java)
            .single()
        handle.createUpdate(
            "delete from INTERVENIENTE where oId = :id;" +
                    "delete from TERMO_ABERTURA where oId = :id;" +
                    "delete from EMPRESA_CONSTRUCAO where id = :companyId;" +
                    "delete from IMAGEM_OBRA where work_id = :id;" +
                    "delete from MEMBRO where oId = :id;" +
                    "delete from OBRA where id = :id"
        )
            .bind("id", workId.toString())
            .bind("companyId", constructionCompanyId)
            .execute()
    }

    override fun getSiteDiary(workId: UUID): SiteDiary = handle.createQuery(
        "select autorizacao, assinatura, dt_assinatura, o.concelho, o.freguesia, o.rua, o.cpostal, predio, " +
                "ec.nome as nome_empresa, ec.numero as numero_empresa, titular_licenca, o.tipo, " +
                "ARRAY(SELECT CONCAT(role, ';', nome, ';', associacao, ';', numero) FROM INTERVENIENTE " +
                "WHERE oId = :id) AS technicians, ARRAY(SELECT CONCAT(texto, ';', u.username, ';', creation_date, " +
                "';', last_modification_date) FROM REGISTO join Utilizador u on u.id = author WHERE oId = :id) " +
                "AS logs from TERMO_ABERTURA ta join Obra o on o.id = ta.oId join empresa_construcao ec on " +
                "ec.id = empresa_construcao where ta.oId = :id"
    )
        .bind("id", workId.toString())
        .mapTo(SiteDiary::class.java)
        .single()

    private fun addCouncilAsMember(workId: UUID, location: Location) {
        val councilId = handle.createQuery(
            "select id from UTILIZADOR where freguesia = :parish and " +
                    "concelho = :county and distrito = :district and role='CÂMARA'"
        )
            .bind("parish", location.parish)
            .bind("county", location.county)
            .bind("district", location.district)
            .mapTo(Int::class.java)
            .singleOrNull()
        if (councilId != null) {
            handle.createUpdate("insert into MEMBRO(uId, oId, role) values(:uId, :oId, :role)")
                .bind("uId", councilId)
                .bind("oId", workId.toString())
                .bind("role", "ESPECTADOR")
        }
    }

    private fun insertTechnicians(technicians: List<Technician>, tId: Int, workId: UUID) {
        val query = StringBuilder("insert into INTERVENIENTE(tId, oId, nome, email, role, associacao, numero) values ")
        technicians.forEach {
            query.append(
                "($tId, '$workId', '${it.name}', '${it.email}', '${it.role}', '${it.association.name}', " +
                        "${it.association.number}), "
            )
        }
        handle.createUpdate(query.toString().dropLast(2)).execute()
    }


}
