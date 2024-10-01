package pt.isel.sitediary.domainmodel.user

import pt.isel.sitediary.domainmodel.work.Association
import pt.isel.sitediary.domainmodel.work.Location

data class User(
    val id: Int,
    val username: String,
    val name: String,
    val nif: Int,
    val email: String,
    val phone: String,
    val role: String,
    val location: Location,
    val association: Association
) {
    fun toMember() = Member(
        id = id,
        name = username,
        role = role
    )
}

data class Password(val passwordValue: String)