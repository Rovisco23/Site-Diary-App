package pt.isel.sitediary.domainmodel.authentication

import pt.isel.sitediary.domainmodel.user.User

data class AuthenticatedUser(
    val user: User,
    val token: String
)