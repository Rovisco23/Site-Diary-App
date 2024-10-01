package pt.isel.sitediary.domain

import android.graphics.Bitmap

data class LoggedUser(
    val userId: Int,
    val username: String,
    val token: String,
    val role: String
)

data class User(
    val id: Int,
    val username: String,
    val nif: Int,
    val email: String,
    val phone: String,
    val firstName: String,
    val lastName: String,
    val role: String,
    val association: Association,
    val location: Location
) {
    fun getName() = "$firstName $lastName"
    fun getPhoneNumber() = phone.ifEmpty { "-" }
}

data class Profile(
    val user: User,
    val profilePicture: Bitmap?
)

data class Member(
    val id: Int,
    val name: String,
    val role: String
)

data class Technician(
    val name: String,
    val role: String,
    val association: Association
)