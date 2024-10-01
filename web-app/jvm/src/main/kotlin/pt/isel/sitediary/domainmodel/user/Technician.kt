package pt.isel.sitediary.domainmodel.user

import pt.isel.sitediary.domainmodel.work.Association

data class Technician(
    val name: String,
    val email: String,
    val role: String,
    val association: Association
)