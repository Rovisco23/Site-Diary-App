package pt.isel.sitediary.repository

import pt.isel.sitediary.domainmodel.work.Location

interface AddressRepository {
    fun getLocation(parish: String, county: String, district: String): Location?
    fun getParishes(county: String, district: String): List<String>
    fun getCountys(district: String): List<String>
    fun getDistricts(): List<String>
}