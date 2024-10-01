package pt.isel.sitediary.domainmodel.authentication

interface TokenEncoder {
    fun createValidationInformation(token: String): TokenValidationInfo
    fun hashPassword(password: String): String
}