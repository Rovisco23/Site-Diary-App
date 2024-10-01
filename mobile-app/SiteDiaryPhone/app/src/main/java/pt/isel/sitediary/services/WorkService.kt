package pt.isel.sitediary.services

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import pt.isel.sitediary.domain.Work
import pt.isel.sitediary.domain.WorkListDto
import pt.isel.sitediary.domain.WorkSimplified
import pt.isel.sitediary.domain.toWorkSimplifiedList
import pt.isel.sitediary.ui.common.GetMainActivityValuesException
import pt.isel.sitediary.ui.common.GetWorkException
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class WorkService(
    private val client: OkHttpClient,
    private val gson: Gson,
    url: String
) {
    private val templateURL = "$url/api"

    suspend fun getAllWork(token: String): List<WorkSimplified> {
        val request = Request.Builder()
            .url("$templateURL/work")
            .header("accept", "application/json")
            .header("Authorization", "Bearer $token")
            .get()
            .build()
        return suspendCoroutine {
            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    it.resumeWithException(GetMainActivityValuesException("Failed to get work list", e))
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    val body = response.body
                    if (!response.isSuccessful || body == null) {
                        if (body != null) {
                            it.resumeWithException(GetMainActivityValuesException(body.string()))
                        } else it.resumeWithException(GetMainActivityValuesException("Failed to get work list"))
                    } else it.resume(
                        gson.fromJson(body.string(), Array<WorkListDto>::class.java)
                            .toWorkSimplifiedList()
                    )
                }
            })
        }
    }

    suspend fun getWork(workId: String, token: String): Work {
        val request = Request.Builder()
            .url("$templateURL/work/$workId")
            .header("accept", "application/json")
            .header("Authorization", "Bearer $token")
            .get()
            .build()
        return suspendCoroutine {
            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    it.resumeWithException(GetWorkException("Failed to get work list", e))
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    val body = response.body
                    if (!response.isSuccessful || body == null) {
                        if (body != null) {
                            it.resumeWithException(GetWorkException(body.string()))
                        } else it.resumeWithException(GetWorkException("Failed to get work list"))
                    } else it.resume(
                        gson.fromJson(body.string(), Work::class.java)
                    )
                }
            })
        }
    }
}