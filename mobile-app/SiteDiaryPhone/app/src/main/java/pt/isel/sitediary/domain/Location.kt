package pt.isel.sitediary.domain

data class Address(
    val location: Location,
    val street: String,
    val postalCode: String
) {
    override fun toString(): String {
        return "$street, $postalCode"
    }
}

data class Location(
    val district: String,
    val county: String,
    val parish: String
) {
    override fun toString(): String {
        return "$parish, $county, $district"
    }
}