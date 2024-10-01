package pt.isel.sitediary.model

import pt.isel.sitediary.domainmodel.user.Technician
import pt.isel.sitediary.domainmodel.user.User
import pt.isel.sitediary.domainmodel.work.Address
import pt.isel.sitediary.domainmodel.work.ConstructionCompany
import java.util.*

data class ListOfWorksOutputModel(val list: List<WorkOutputModel>)

data class WorkOutputModel(
    val id: UUID,
    val name: String,
    val description: String,
    val address: Address
)

data class EditWorkInputModel(
    val name: String,
    val type: String,
    val description: String,
    val licenseHolder: String,
    val company: ConstructionCompany,
    val building: String,
    val address: Address,
    val technicians: List<Technician>
)

data class OpeningTermInputModel(
    val name: String,
    val type: String,
    val description: String?,
    val holder: String,
    val company: ConstructionCompany,
    val building: String,
    val address: Address,
    val technicians: List<Technician>,
    val verification: String?
) {
    fun checkParams() =
        name.isBlank() || holder.isBlank() || company.name.isBlank() || company.num <= 0 ||
                building.isBlank() || address.location.parish?.isBlank() ?: true || address.street.isBlank() ||
                address.postalCode.isBlank() || address.location.county?.isBlank() ?: true

    fun checkTechnicians() =
        technicians.filter { it.role == "DIRETOR" || it.role == "FISCALIZAÇÃO" || it.role == "COORDENADOR" }.size < 3

    fun checkCouncilWork(user: User): Boolean =
        address.location.district == user.location.district && address.location.county == user.location.county
                && user.role == "CÂMARA"
}

data class MemberInputModel(
    val email: String,
    val role: String
)

data class InviteInputModel(
    val workId: UUID,
    val accepted: Boolean
)

data class PendingInputModel(
    val userId: Int,
    val accepted: Boolean
)