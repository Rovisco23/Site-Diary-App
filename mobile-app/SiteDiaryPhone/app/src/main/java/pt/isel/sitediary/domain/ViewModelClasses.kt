package pt.isel.sitediary.domain

data class MainValues(
    val workList: List<WorkSimplified>,
    val logs: List<LogEntrySimplified>,
    val selectedLog: LogEntry? = null,
    val profile: Profile
)