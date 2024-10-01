package pt.isel.sitediary.repository

import pt.isel.sitediary.domainmodel.work.LogEntry
import pt.isel.sitediary.domainmodel.work.OwnLogSimplified
import pt.isel.sitediary.model.FileModel
import pt.isel.sitediary.model.LogInputModel
import java.sql.Timestamp
import java.util.*

interface LogRepository {
    fun createLog(
        log: LogInputModel,
        createdAt: Timestamp,
        author: Int,
        images: List<FileModel>?,
        docs: List<FileModel>?
    ): Int
    fun getById(id: Int): LogEntry?
    fun checkUserAccess(workId: UUID, userId: Int): Boolean
    fun getFiles(images: List<Int>, documents: List<Int>): List<FileModel>?
    fun finish(logId: Int)
    fun editLog(
        logId: Int,
        logInfo: LogInputModel,
        modifiedAt: Timestamp,
        images: List<FileModel>?,
        docs: List<FileModel>?
    )
    fun deleteFiles(images: List<Int>, documents: List<Int>)
    fun getMyLogs(userId: Int): List<OwnLogSimplified>
    fun deleteImage(fileId: Int)
    fun deleteDocument(fileId: Int)
    fun deleteLog(workId: UUID)
}