package pt.isel.sitediary.domainmodel.work

data class Address(
    val location: Location,
    val street: String,
    val postalCode: String
)

data class Location(
    val district: String?,
    val county: String?,
    val parish: String?
)