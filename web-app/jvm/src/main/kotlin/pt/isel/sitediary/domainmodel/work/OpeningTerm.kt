package pt.isel.sitediary.domainmodel.work

data class OpeningTermVerification(
    val doc: String,
    val signature: String,
    val dt_signature: String
)

data class OpeningTermLocation(
    val county: String,
    val parish: String,
    val street: String,
    val postalCode: String,
    val building: String
)

data class OpeningTermAuthor(
    val name: String,
    val association: String,
    val num: Int
)

data class OpeningTerm(
    val verification: OpeningTermVerification,
    val location: OpeningTermLocation,
    val licenseHolder: String,
    val authors: Map<String, OpeningTermAuthor>,
    val company: ConstructionCompany,
    val type: String,
)

data class SiteDiaryLog(
    val content: String,
    val author: String,
    val createdAt: String,
    val lastModificationAt: String
)

data class SiteDiary(
    val verification: OpeningTermVerification,
    val location: OpeningTermLocation,
    val licenseHolder: String,
    val authors: Map<String, OpeningTermAuthor>,
    val logs: List<SiteDiaryLog>,
    val company: ConstructionCompany,
    val type: String,
) {
    fun toOpeningTerm(): OpeningTerm = OpeningTerm(
        verification = verification,
        location = location,
        licenseHolder = licenseHolder,
        authors = authors,
        company = company,
        type = type
    )
}