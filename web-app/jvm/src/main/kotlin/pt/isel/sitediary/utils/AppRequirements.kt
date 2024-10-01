package pt.isel.sitediary.utils

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin
import pt.isel.sitediary.repository.mappers.FileModelMapper
import pt.isel.sitediary.repository.mappers.GetUserMapper
import pt.isel.sitediary.repository.mappers.InviteMapper
import pt.isel.sitediary.repository.mappers.InviteSimplifiedMapper
import pt.isel.sitediary.repository.mappers.LocationMapper
import pt.isel.sitediary.repository.mappers.LogEntryMapper
import pt.isel.sitediary.repository.mappers.MemberProfileMapper
import pt.isel.sitediary.repository.mappers.OpeningTermMapper
import pt.isel.sitediary.repository.mappers.OwnLogSimplifiedMapper
import pt.isel.sitediary.repository.mappers.PendingCouncilsMapper
import pt.isel.sitediary.repository.mappers.SiteDiaryMapper
import pt.isel.sitediary.repository.mappers.UserAndTokenMapper
import pt.isel.sitediary.repository.mappers.WorkMapper
import pt.isel.sitediary.repository.mappers.WorkSimplifiedMapper
import pt.isel.sitediary.repository.mappers.WorkVerifyingMapper

fun Jdbi.configureWithAppRequirements(): Jdbi {
    installPlugin(KotlinPlugin())
    installPlugin(PostgresPlugin())

    registerRowMapper(GetUserMapper())
    registerRowMapper(LocationMapper())
    registerRowMapper(UserAndTokenMapper())
    registerRowMapper(WorkMapper())
    registerRowMapper(WorkSimplifiedMapper())
    registerRowMapper(WorkVerifyingMapper())
    registerRowMapper(LogEntryMapper())
    registerRowMapper(OwnLogSimplifiedMapper())
    registerRowMapper(OpeningTermMapper())
    registerRowMapper(SiteDiaryMapper())
    registerRowMapper(FileModelMapper())
    registerRowMapper(MemberProfileMapper())
    registerRowMapper(InviteMapper())
    registerRowMapper(InviteSimplifiedMapper())
    registerRowMapper(PendingCouncilsMapper())
    return this
}