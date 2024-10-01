package pt.isel.sitediary.ui.main.mylogs

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import pt.isel.sitediary.domain.LoadState
import pt.isel.sitediary.domain.Loaded
import pt.isel.sitediary.domain.MainValues
import pt.isel.sitediary.ui.common.LoadingScreen
import pt.isel.sitediary.ui.work.log.LogScreen
import pt.isel.sitediary.ui.work.log.LogsScreen

@Composable
fun MyLogsView(
    logs: LoadState<MainValues>,
    innerPadding: PaddingValues,
    onLogSelected: (Int) -> Unit = {},
    onLogBackRequested: () -> Unit = {},
    onUploadRequested: () -> Unit = {},
    onDeleteSubmit: (Int, String) -> Unit,
    onEditSubmit: (String) -> Unit = {}
) {
    logs.let {
        if (it is Loaded && it.value.isSuccess) {
            it.value.getOrNull()?.let { values ->
                if (values.selectedLog != null) {
                    LogScreen(
                        values.selectedLog,
                        innerPadding,
                        onLogBackRequested,
                        onUploadRequested,
                        onDeleteSubmit,
                        onEditSubmit
                    )
                } else {
                    LogsScreen(
                        values.logs,
                        innerPadding,
                        onLogSelected,
                        needTopBar = false
                    )
                }
            }
        } else {
            LoadingScreen()
        }
    }
}