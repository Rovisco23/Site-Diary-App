package pt.isel.sitediary.repository.jdbi

import org.jdbi.v3.core.Handle
import pt.isel.sitediary.domainmodel.work.LogEntry
import pt.isel.sitediary.domainmodel.work.OwnLogSimplified
import pt.isel.sitediary.model.FileModel
import pt.isel.sitediary.model.LogInputModel
import pt.isel.sitediary.repository.LogRepository
import java.sql.Timestamp
import java.util.*

class JdbiLog(private val handle: Handle) : LogRepository {

    override fun createLog(
        log: LogInputModel,
        createdAt: Timestamp,
        author: Int,
        images: List<FileModel>?,
        docs: List<FileModel>?
    ): Int {
        val rId = handle.createUpdate(
            "insert into registo(oId, texto, editable, creation_date, last_modification_date, author)" +
                    "values (:workId, :description, :editable, :createdAt, :createdAt, :author)"
        )
            .bind("workId", log.workId)
            .bind("description", log.description)
            .bind("editable", true)
            .bind("createdAt", createdAt)
            .bind("author", author)
            .executeAndReturnGeneratedKeys()
            .mapTo(Int::class.java)
            .one()
        images?.forEach { img -> inputImagesToLog(rId, log.workId, img, createdAt) }
        docs?.forEach { doc -> inputDocsToLog(rId, log.workId, doc, createdAt) }
        return rId
    }

    override fun getById(id: Int): LogEntry? = handle.createQuery(
        "select distinct REGISTO.id, REGISTO.oid, texto, editable, creation_date, last_modification_date, " +
                "author, username, (SELECT Membro.role from Membro join Registo r on r.oId = Membro.oId where " +
                "Membro.uid = author and r.id = :id), ARRAY(SELECT CONCAT(id, ';', name, ';', 'Imagem', ';', " +
                "upload_date) from IMAGEM where rId = :id) as images, ARRAY(SELECT CONCAT(id, ';', name, ';', " +
                "'Documento', ';', upload_date) from DOCUMENTO where rId = :id) as documents from REGISTO join " +
                "UTILIZADOR on UTILIZADOR.id = author where Registo.id = :id"
    )
        .bind("id", id)
        .mapTo(LogEntry::class.java)
        .singleOrNull()

    override fun getMyLogs(userId: Int): List<OwnLogSimplified> =
        handle.createQuery(
            "SELECT r.id, r.oId, o.nome, (SELECT distinct u.username from Utilizador u join Registo on " +
                    "Registo.author = u.id where Registo.id = r.id) as author, r.editable, COUNT(i.name) > 0 OR " +
                    "COUNT(d.name) > 0 as attachments, TO_CHAR(r.creation_date, 'YYYY-MM-DD') as createdAt from " +
                    "REGISTO r join Membro on Membro.oId = r.oId join OBRA o on r.oId = o.id LEFT JOIN IMAGEM i ON " +
                    "i.rId = r.id LEFT JOIN DOCUMENTO d ON d.rId = r.id where Membro.uId = :id group by r.id, r.oId, " +
                    "o.nome, r.editable, r.creation_date order by r.creation_date"
        )
            .bind("id", userId)
            .mapTo(OwnLogSimplified::class.java)
            .list()

    override fun deleteImage(fileId: Int) {
        handle.createUpdate(
            "delete from IMAGEM where id = :id"
        )
            .bind("id", fileId)
            .execute()
    }

    override fun deleteDocument(fileId: Int) {
        handle.createUpdate(
            "delete from DOCUMENTO where id = :id"
        )
            .bind("id", fileId)
            .execute()
    }

    override fun deleteLog(workId: UUID) {
        handle.createUpdate(
            "delete from IMAGEM where oId = :id;" +
            "delete from DOCUMENTO where oId = :id;" +
            "delete from REGISTO where oId = :id"
        )
            .bind("id", workId.toString())
            .execute()
    }

    override fun checkUserAccess(workId: UUID, userId: Int): Boolean = handle.createQuery(
        "select count(*) from MEMBRO where oId = :oId and uId = :uId and pendente = :pendente"
    )
        .bind("oId", workId.toString())
        .bind("uId", userId)
        .bind("pendente", false)
        .mapTo(Int::class.java)
        .single() == 1

    override fun getFiles(images: List<Int>, documents: List<Int>): List<FileModel>? {
        val files = mutableListOf<FileModel>()
        images.forEach {
            files.add(
                handle.createQuery(
                    "select name, type, file from IMAGEM where id = :id"
                )
                    .bind("id", it)
                    .mapTo(FileModel::class.java)
                    .single()
            )
        }
        documents.forEach {
            files.add(
                handle.createQuery(
                    "select name, type, file from DOCUMENTO where id = :id"
                )
                    .bind("id", it)
                    .mapTo(FileModel::class.java)
                    .single()
            )
        }
        return files.ifEmpty { null }
    }

    override fun finish(logId: Int) {
        handle.createUpdate(
            "update REGISTO set editable = :editable where id = :id"
        )
            .bind("id", logId)
            .bind("editable", false)
            .execute()
    }

    override fun editLog(
        logId: Int,
        logInfo: LogInputModel,
        modifiedAt: Timestamp,
        images: List<FileModel>?,
        docs: List<FileModel>?
    ) {
        handle.createUpdate(
            "update REGISTO set texto = :description, last_modification_date = :modDate " +
                    "where id = :id"
        )
            .bind("description", logInfo.description)
            .bind("modDate", Timestamp(System.currentTimeMillis()))
            .bind("id", logId)
            .execute()
        images?.forEach { img -> inputImagesToLog(logId, logInfo.workId, img, modifiedAt) }
        docs?.forEach { doc -> inputDocsToLog(logId, logInfo.workId, doc, modifiedAt) }
    }

    override fun deleteFiles(images: List<Int>, documents: List<Int>) {
        images.forEach {
            handle.createUpdate(
                "delete from IMAGEM where id = :id"
            )
                .bind("id", it)
                .execute()
        }
        documents.forEach {
            handle.createUpdate(
                "delete from DOCUMENTO where id = :id"
            )
                .bind("id", it)
                .execute()
        }
    }

    private fun inputImagesToLog(rId: Int, workId: UUID, images: FileModel, uploadDate: Timestamp) {
        handle.createUpdate(
            "insert into IMAGEM(rId, oId, name, type, file, upload_date) values " +
                    "(:rId, :oId, :name, :type, :file, :upload_date)"
        )
            .bind("rId", rId)
            .bind("oId", workId)
            .bind("name", images.fileName)
            .bind("type", images.contentType)
            .bind("file", images.file)
            .bind("upload_date", uploadDate)
            .execute()
    }

    private fun inputDocsToLog(rId: Int, workId: UUID, doc: FileModel, uploadDate: Timestamp) {
        handle.createUpdate(
            "insert into DOCUMENTO(rId, oId, name, type, file, upload_date) values " +
                    "(:rId, :oId, :name, :type, :file, :upload_date)"
        )
            .bind("rId", rId)
            .bind("oId", workId)
            .bind("name", doc.fileName)
            .bind("type", doc.contentType)
            .bind("file", doc.file)
            .bind("upload_date", uploadDate)
            .execute()
    }
}