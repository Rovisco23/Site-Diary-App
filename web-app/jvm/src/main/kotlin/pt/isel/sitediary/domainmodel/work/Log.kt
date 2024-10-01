package pt.isel.sitediary.domainmodel.work

import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import pt.isel.sitediary.model.FileOutputModel
import java.time.Duration
import java.util.*

data class LogEntry(
    val id: Int,
    val workId: UUID,
    val author: Author,
    val content: String,
    val editable: Boolean,
    val createdAt: Date,
    val lastModifiedAt: Date,
    val files: List<FileOutputModel>
) {
    fun checkIfEditTimeElapsed(clock: Clock): Boolean {
        val elapsedTime = Duration.between(createdAt.toInstant(), clock.now().toJavaInstant()).toMillis()
        return elapsedTime >= Duration.ofHours(3).toMillis()
    }
}

data class LogEntrySimplified(
    val id: Int,
    val author: Author,
    val editable: Boolean,
    val createdAt: Date,
    val attachments: Boolean
) {
    fun checkIfEditTimeElapsed(clock: Clock): Boolean {
        val elapsedTime = Duration.between(createdAt.toInstant(), clock.now().toJavaInstant()).toMillis()
        return elapsedTime >= Duration.ofHours(3).toMillis()
    }
}

data class OwnLogSimplified(
    val id: Int,
    val workId: UUID,
    val workName: String,
    val author: String,
    val editable: Boolean,
    val createdAt: Date,
    val attachments: Boolean
)