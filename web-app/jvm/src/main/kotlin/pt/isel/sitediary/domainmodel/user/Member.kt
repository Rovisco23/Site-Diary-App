package pt.isel.sitediary.domainmodel.user

data class Member(
    val id: Int,
    val name: String,
    val role: String
) {
    fun checkTechnician() = role != "MEMBRO" && role != "ESPECTADOR"
}

fun List<Member>.containsMemberById(userId: Int) = this.any { it.id == userId }

fun List<Member>.checkOwner(userId: Int) = this.any { it.id == userId && it.role == "DONO"}
