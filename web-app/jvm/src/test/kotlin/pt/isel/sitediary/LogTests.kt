package pt.isel.sitediary

import kotlinx.datetime.Clock
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pt.isel.sitediary.domainmodel.work.Author
import pt.isel.sitediary.domainmodel.work.LogEntry
import pt.isel.sitediary.domainmodel.work.LogEntrySimplified
import pt.isel.sitediary.model.FileOutputModel
import pt.isel.sitediary.model.LogCredentialsModel
import java.time.Instant
import java.util.*

class LogTests {
    private val createTime = Date.from(Instant.now())
    private val elapsedTime = Date.from(Instant.now().minusMillis(20000000))
    private val workId = UUID.randomUUID()
    private val clock = Clock.System
    private val log = LogEntry(
        id = 1,
        workId = workId,
        author = Author(
            id = 1,
            name = "John Doe",
            role = "FISCALIZAÇÃO"
        ),
        content = "This is a log entry.",
        editable = true,
        createdAt = createTime,
        lastModifiedAt = createTime,
        files = emptyList()
    )

    private val expiredLog = LogEntry(
        id = 1,
        workId = workId,
        author = Author(
            id = 1,
            name = "John Doe",
            role = ""
        ),
        content = "This is a log entry.",
        editable = true,
        createdAt = elapsedTime,
        lastModifiedAt = createTime,
        files = emptyList()
    )

    private val simplifiedLog = LogEntrySimplified(
        id = 1,
        author = Author(
            id = 1,
            name = "John Doe",
            role = "FISCALIZAÇÃO"
        ),
        editable = true,
        createdAt = createTime,
        attachments = false
    )

    private val expiredSimplifiedLog = LogEntrySimplified(
        id = 1,
        author = Author(
            id = 1,
            name = "John Doe",
            role = "FISCALIZAÇÃO"
        ),
        editable = true,
        createdAt = elapsedTime,
        attachments = false
    )

    private val inputLog = LogCredentialsModel(
        logId = 1,
        workId = workId,
        files = listOf(
            FileOutputModel(
                id = 1,
                fileName = "file1",
                contentType = "Imagem",
                uploadDate = createTime
            ),
            FileOutputModel(
                id = 2,
                fileName = "file2",
                contentType = "Imagem",
                uploadDate = createTime
            ),
            FileOutputModel(
                id = 3,
                fileName = "file3",
                contentType = "Documento",
                uploadDate = createTime
            ),
            FileOutputModel(
                id = 4,
                fileName = "file4",
                contentType = "Documento",
                uploadDate = createTime
            )
        )
    )

    @Test
    fun `Check if time to edit log has elapsed`() {
        assertTrue(!log.checkIfEditTimeElapsed(clock))
    }

    @Test
    fun `Check if time to edit log hasn't elapsed`() {
        assertTrue(expiredLog.checkIfEditTimeElapsed(clock))
    }

    @Test
    fun `Check if time to edit log in a simplified state has elapsed`() {
        assertTrue(!simplifiedLog.checkIfEditTimeElapsed(clock))
    }

    @Test
    fun `Check if time to edit log in a simplified state hasn't elapsed`() {
        assertTrue(expiredSimplifiedLog.checkIfEditTimeElapsed(clock))
    }

    @Test
    fun `Divide log files correctly`() {
        val (images, documents) = inputLog.filterFiles()
        assertTrue(images.size == 2)
        assertTrue(documents.size == 2)
    }
}