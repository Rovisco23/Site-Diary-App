package pt.isel.sitediary.ui.work.log

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import pt.isel.sitediary.domain.LogValues

@Composable
fun LogsView(
    logValues: LogValues,
    innerPadding: PaddingValues,
    onLogSelected: (Int) -> Unit = {},
    onBackRequested: () -> Unit = {},
    onLogBackRequested: () -> Unit = {},
    onUploadRequested: () -> Unit = {},
    onDeleteSubmit: (Int, String) -> Unit,
    onEditSubmit: (String) -> Unit = {}
) {
    if (logValues.selectedLog != null) {
        LogScreen(
            logValues.selectedLog,
            innerPadding,
            onLogBackRequested,
            onUploadRequested,
            onDeleteSubmit,
            onEditSubmit
        )
    } else {
        LogsScreen(
            logValues.logs,
            innerPadding,
            onLogSelected,
            onBackRequested
        )
    }
}
