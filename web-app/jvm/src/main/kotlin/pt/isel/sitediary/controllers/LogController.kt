package pt.isel.sitediary.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import pt.isel.sitediary.domainmodel.authentication.AuthenticatedUser
import pt.isel.sitediary.domainmodel.work.LogEntry
import pt.isel.sitediary.model.DeleteFileModel
import pt.isel.sitediary.model.FileModel
import pt.isel.sitediary.model.LogCredentialsModel
import pt.isel.sitediary.model.LogInputModel
import pt.isel.sitediary.model.LogOutputModel
import pt.isel.sitediary.service.LogService
import pt.isel.sitediary.utils.Errors
import pt.isel.sitediary.utils.Paths
import pt.isel.sitediary.utils.handleResponse
import java.io.ByteArrayOutputStream
import java.net.URI
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RestController
@Tag(name = "Log", description = "Operations related the Logs.")
class LogController(private val service: LogService) {

    @PostMapping(Paths.Log.GET_ALL_LOGS, consumes = ["multipart/form-data"])
    @Operation(summary = "Create Log", description = "Used to create a log for a specific work.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201", description = "Successful Log Creation",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = Unit::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401", description = "Not a Member of the Work",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = Errors::class))
                ]
            )
        ]
    )
    fun createLog(
        @RequestPart("log") log: LogInputModel,
        @RequestPart("files", required = false) files: List<MultipartFile>?,
        @Parameter(hidden = true) user: AuthenticatedUser
    ): ResponseEntity<*> {
        val listOfFiles = files?.map {
            FileModel(
                fileName = it.originalFilename!!,
                contentType = it.contentType!!,
                file = it.bytes
            )
        }
        val res = service.createLog(log, listOfFiles, user.user.id)
        return handleResponse(res) {
            ResponseEntity.created(URI.create("/log-entry/$it")).body(Unit)
        }
    }

    @GetMapping(Paths.Log.GET_BY_ID)
    @Operation(summary = "Get Log By Id", description = "Used to get the details of a log")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Log received successfully",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = LogEntry::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401", description = "Not a Member of the Work",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = Errors::class))
                ]
            )
        ]
    )
    fun getLogById(@PathVariable id: Int, @Parameter(hidden = true) user: AuthenticatedUser): ResponseEntity<*> {
        val res = service.getLog(id, user.user)
        return handleResponse(res) {
            val log = LogOutputModel(
                id = it.id,
                workId = it.workId,
                content = it.content,
                editable = it.editable,
                createdAt = it.createdAt,
                modifiedAt = it.lastModifiedAt,
                author = it.author,
                files = it.files
            )
            ResponseEntity.ok(log)
        }
    }

    @GetMapping(Paths.Log.GET_MY_LOGS)
    @Operation(summary = "Get user's logs", description = "Used to get a list with a user's logs")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Log received successfully",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = LogEntry::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401", description = "Not a Member of the Work",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = Errors::class))
                ]
            )
        ]
    )
    fun getMyLogs(@Parameter(hidden = true) user: AuthenticatedUser): ResponseEntity<*> {
        val res = service.getMyLogs(user.user)
        return handleResponse(res) {
            ResponseEntity.ok(it)
        }
    }

    @PutMapping(Paths.Log.EDIT_LOG, consumes = ["multipart/form-data"])
    @Operation(
        summary = "Edit the content of the Log",
        description = "Used to edit the content of a log while it is still editable."
    )
    fun editLog(
        @PathVariable id: Int,
        @RequestPart("log") log: LogInputModel,
        @RequestPart("files", required = false) files: List<MultipartFile>?,
        @Parameter(hidden = true) user: AuthenticatedUser
    ): ResponseEntity<*> {
        val listOfFiles = files?.map {
            FileModel(
                fileName = it.originalFilename!!,
                contentType = it.contentType!!,
                file = it.bytes
            )
        }
        val res = service.editLog(id, log, listOfFiles, user.user.id)
        return handleResponse(res) {
            ResponseEntity.ok().header("Location", "/log-entry/$id").body(Unit)
        }
    }

    @PostMapping(Paths.Log.GET_LOG_FILES)
    @Operation(summary = "Get Log Files", description = "Used to get the image or document files of a log")
    fun getLogFiles(
        @RequestBody log: LogCredentialsModel,
        @Parameter(hidden = true) user: AuthenticatedUser
    ): ResponseEntity<*> {
        val res = service.getLogFiles(log, user.user.id)
        return handleResponse(res) {
            if (it == null) ResponseEntity.ok().body(null)
            else {
                val zipBytes = makeZip(it)
                val zipFileName = "files.zip"
                ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=$zipFileName")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(ByteArrayResource(zipBytes))
            }
        }
    }

    @PostMapping(Paths.Log.DELETE_FILES)
    @Operation(summary = "Delete Log Files", description = "Used to delete one or more files from a log.")
    fun deleteFiles(
        @RequestBody body: LogCredentialsModel,
        @Parameter(hidden = true) user: AuthenticatedUser
    ): ResponseEntity<*> {
        val res = service.deleteFiles(body, user.user.id)
        return handleResponse(res) {
            ResponseEntity.ok().body(Unit)
        }
    }

    @PostMapping(Paths.Log.DELETE_FILE)
    @Operation(summary = "Delete Log File", description = "Used to delete a file from a log.")
    fun deleteFile(
        @RequestBody body: DeleteFileModel,
        @Parameter(hidden = true) user: AuthenticatedUser
    ): ResponseEntity<*> {
        val res = service.deleteFile(body, user.user.id)
        return handleResponse(res) {
            ResponseEntity.ok().body(Unit)
        }
    }

    private fun makeZip(files: List<FileModel>): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        ZipOutputStream(byteArrayOutputStream).use { zipOut ->
            files.forEach { file ->
                val zipEntry = ZipEntry(file.fileName)
                zipOut.putNextEntry(zipEntry)
                zipOut.write(file.file)
                zipOut.closeEntry()
            }
        }
        return byteArrayOutputStream.toByteArray()
    }
}