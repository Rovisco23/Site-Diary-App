package pt.isel.sitediary.domain

import java.io.File
import java.util.Date
import java.util.UUID

data class LogEntry(
    val id: Int,
    val workId: UUID,
    val author: Author,
    val content: String,
    val editable: Boolean,
    val createdAt: Date,
    val modifiedAt: Date,
    val files: List<FileModel>
)

data class FileModel(
    val id: Int,
    val fileName: String,
    val contentType: String
)

data class DeleteFileModel(
    val logId: Int,
    val fileId: Int,
    val type: String
)

data class LogEntrySimplified(
    val id: Int,
    val author: Author,
    val createdAt: Date,
    val editable: Boolean,
    val attachments: Boolean
)

data class LogValues(val logs: List<LogEntrySimplified>, val selectedLog: LogEntry?)

data class Author(
    val id: Int,
    val name: String,
    val role: String
)

data class LogInputModel(
    val description: String,
    val selectedFiles: HashMap<String, File>
)

data class UploadInput(
    val logId: Int,
    val workId: String,
    val description: String,
    val selectedFiles: HashMap<String, File>
)
