package pt.isel.sitediary.services

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import pt.isel.sitediary.ui.common.LoginException
import pt.isel.sitediary.domain.LoggedUser
import pt.isel.sitediary.domain.User
import pt.isel.sitediary.ui.common.EditProfileException
import pt.isel.sitediary.ui.common.LogException
import pt.isel.sitediary.ui.common.ProfileException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class UserService(
    private val client: OkHttpClient,
    private val gson: Gson,
    url: String
) {

    private val templateURL = "$url/api"

    suspend fun login(username: String, password: String): LoggedUser {
        val request = Request.Builder()
            .url("$templateURL/login")
            .header("accept", "application/json")
            .post(
                """{
                "user": "$username",
                "password": "$password"
                }""".trimMargin().toRequestBody("application/json".toMediaTypeOrNull())
            )
            .build()
        return suspendCoroutine {
            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    it.resumeWithException(LoginException("Failed to login", e))
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    val body = response.body
                    if (!response.isSuccessful || body == null) {
                        if (body != null) {
                            it.resumeWithException(LoginException(body.string()))
                        } else it.resumeWithException(LoginException("Failed to login"))
                    } else it.resume(gson.fromJson(body.string(), LoggedUser::class.java))
                }
            })
        }
    }

    suspend fun getProfile(id: Int, token: String): User {
        val request = Request.Builder()
            .url("$templateURL/users/$id")
            .header("accept", "application/json")
            .header("Authorization", "Bearer $token")
            .get()
            .build()
        return suspendCoroutine {
            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    it.resumeWithException(ProfileException("Failed to get profile", e))
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    val body = response.body
                    if (!response.isSuccessful || body == null) {
                        if (body != null) {
                            it.resumeWithException(ProfileException(body.string()))
                        } else it.resumeWithException(ProfileException("Failed to get profile"))
                    } else it.resume(gson.fromJson(body.string(), User::class.java))
                }
            })
        }
    }

    suspend fun getProfilePicture(id: Int, token: String): Bitmap? {
        val request = Request.Builder()
            .url("$templateURL/profile-picture/$id")
            .header("accept", "application/octet-stream")
            .header("Authorization", "Bearer $token")
            .get()
            .build()
        return suspendCoroutine {
            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    it.resumeWithException(ProfileException("Failed to get profile", e))
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    val body = response.body
                    if (!response.isSuccessful || body == null) {
                        if (body != null) {
                            it.resumeWithException(ProfileException(body.string()))
                        } else it.resumeWithException(ProfileException("Failed to get profile"))
                    } else {
                        val bytesArray = body.bytes()
                        if (bytesArray.isEmpty()) {
                            it.resume(null)
                        } else {
                            val bitmap =
                                BitmapFactory.decodeByteArray(bytesArray, 0, bytesArray.size)
                            it.resume(bitmap)
                        }
                    }
                }
            })
        }
    }

    suspend fun editProfilePicture(fileName: String, file: File, token: String) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                fileName,
                file.asRequestBody("image/jpg".toMediaTypeOrNull())
            )
            .build()
        val request = Request.Builder()
            .url("$templateURL/profile-picture")
            .header("accept", "application/json")
            .header("Authorization", "Bearer $token")
            .put(requestBody)
            .build()
        return suspendCoroutine {
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    it.resumeWithException(EditProfileException("Failed to edit Log", e))
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body
                    if (!response.isSuccessful) {
                        if (body != null) {
                            it.resumeWithException(EditProfileException(body.string()))
                        } else it.resumeWithException(EditProfileException("Failed to edit Log"))
                    } else it.resume(Unit)
                }
            })
        }
    }
}