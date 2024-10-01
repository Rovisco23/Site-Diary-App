package pt.isel.sitediary.services

import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import pt.isel.sitediary.domain.Address
import pt.isel.sitediary.domain.Author
import pt.isel.sitediary.domain.DeleteFileModel
import pt.isel.sitediary.domain.LogEntry
import pt.isel.sitediary.domain.LogEntrySimplified
import pt.isel.sitediary.domain.LogInputModel
import pt.isel.sitediary.domain.UploadInput
import pt.isel.sitediary.domain.WorkListDto
import pt.isel.sitediary.domain.WorkSimplified
import pt.isel.sitediary.domain.WorkState
import pt.isel.sitediary.ui.common.LogCreationException
import pt.isel.sitediary.ui.common.LogException
import java.io.IOException
import java.sql.Date
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class LogService(
    private val client: OkHttpClient,
    private val gson: Gson,
    url: String
) {
    private val templateURL = "$url/api"

    suspend fun getLogById(id: Int, token: String): LogEntry {
        val request = Request.Builder()
            .url("$templateURL/logs/$id")
            .header("accept", "application/json")
            .header("Authorization", "Bearer $token")
            .get()
            .build()
        return suspendCoroutine {
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    it.resumeWithException(LogException("Failed to get Log", e))
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body
                    if (!response.isSuccessful || body == null) {
                        if (body != null) {
                            it.resumeWithException(LogException(body.string()))
                        } else it.resumeWithException(LogException("Failed to get Log"))
                    } else it.resume(gson.fromJson(body.string(), LogEntry::class.java))
                }
            })
        }
    }

    suspend fun createLog(input: LogInputModel, workId: String, token: String) {
        val requestBodyBuilder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "log",
                null,
                """{
                "workId": "$workId",
                "description": "${input.description}"
                }""".trimMargin().toRequestBody("application/json".toMediaTypeOrNull())
            )
        input.selectedFiles.forEach { (fileName, file) ->
            requestBodyBuilder.addFormDataPart(
                "files",
                fileName,
                file.asRequestBody("image/*".toMediaTypeOrNull())
            )
        }

        val request = Request.Builder()
            .url("$templateURL/logs")
            .header("accept", "application/json")
            .header("Authorization", "Bearer $token")
            .post(requestBodyBuilder.build())
            .build()
        return suspendCoroutine {
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    it.resumeWithException(LogCreationException("Failed to get Log", e))
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body
                    if (!response.isSuccessful) {
                        if (body != null) {
                            it.resumeWithException(LogCreationException(body.string()))
                        } else it.resumeWithException(LogCreationException("Failed to get Log"))
                    } else it.resume(Unit)
                }
            })
        }
    }

    suspend fun uploadFiles(input: UploadInput, token: String) {
        val requestBodyBuilder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "log",
                null,
                """{
                "workId": "${input.workId}",
                "description": "${input.description}"
                }""".trimMargin().toRequestBody("application/json".toMediaTypeOrNull())
            )
        input.selectedFiles.forEach { (fileName, file) ->
            requestBodyBuilder.addFormDataPart(
                "files",
                fileName,
                file.asRequestBody("image/*".toMediaTypeOrNull())
            )
        }

        val request = Request.Builder()
            .url("$templateURL/logs/${input.logId}")
            .header("accept", "application/json")
            .header("Authorization", "Bearer $token")
            .put(requestBodyBuilder.build())
            .build()

        return suspendCoroutine {
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    it.resumeWithException(LogException("Failed to edit Log", e))
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body
                    if (!response.isSuccessful) {
                        if (body != null) {
                            it.resumeWithException(LogException(body.string()))
                        } else it.resumeWithException(LogException("Failed to edit Log"))
                    } else it.resume(Unit)
                }
            })
        }
    }

    suspend fun deleteFile(file: DeleteFileModel, token: String) {
        val request = Request.Builder()
            .url("$templateURL/delete-file")
            .header("accept", "application/json")
            .header("Authorization", "Bearer $token")
            .post(
                """{
                "logId": "${file.logId}",
                "fileId": "${file.fileId}",
                "type": "${file.type}"
                }""".trimMargin().toRequestBody("application/json".toMediaTypeOrNull())
            )
            .build()
        return suspendCoroutine {
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    it.resumeWithException(LogException("Failed to delete file", e))
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body
                    if (!response.isSuccessful) {
                        if (body != null) {
                            it.resumeWithException(LogException(body.string()))
                        } else it.resumeWithException(LogException("Failed to delete file"))
                    } else it.resume(Unit)
                }
            })
        }
    }

    suspend fun editLog(logId: Int, workId: String, content: String, token: String) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "log",
                null,
                """{
                "workId": "$workId",
                "description": "$content"
                }""".trimMargin().toRequestBody("application/json".toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url("$templateURL/logs/$logId")
            .header("accept", "application/json")
            .header("Authorization", "Bearer $token")
            .put(requestBody)
            .build()

        return suspendCoroutine {
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    it.resumeWithException(LogException("Failed to edit Log", e))
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body
                    if (!response.isSuccessful) {
                        if (body != null) {
                            it.resumeWithException(LogException(body.string()))
                        } else it.resumeWithException(LogException("Failed to edit Log"))
                    } else it.resume(Unit)
                }
            })
        }
    }

    suspend fun getAllLogs(token: String): List<LogEntrySimplified> {
        val request = Request.Builder()
            .url("$templateURL/my-logs")
            .header("accept", "application/json")
            .header("Authorization", "Bearer $token")
            .get()
            .build()
        return suspendCoroutine {
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    it.resumeWithException(LogException("Failed to get Log", e))
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body
                    if (!response.isSuccessful || body == null) {
                        if (body != null) {
                            it.resumeWithException(LogException(body.string()))
                        } else it.resumeWithException(LogException("Failed to get Log"))
                    } else {
                        it.resume(
                            gson.fromJson(
                                body.string(),
                                Array<LogEntryDTO>::class.java
                            ).toLogEntrySimplifiedList()
                        )
                    }
                }
            })
        }
    }
}

data class LogEntryDTO(
    val id: String,
    val author: String,
    val createdAt: String,
    val editable: String,
    val attachments: String
)

fun Array<LogEntryDTO>.toLogEntrySimplifiedList() = map {
    LogEntrySimplified(
        id = it.id.toInt(),
        author = Author(
            id = 0,
            name = it.author,
            role = ""
        ),
        createdAt = Date.valueOf(it.createdAt),
        editable = it.editable.toBoolean(),
        attachments = it.attachments.toBoolean()
    )
}