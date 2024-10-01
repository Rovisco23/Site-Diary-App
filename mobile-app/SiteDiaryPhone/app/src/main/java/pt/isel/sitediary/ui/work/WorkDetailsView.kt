package pt.isel.sitediary.ui.work

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pt.isel.sitediary.domain.LoadState
import pt.isel.sitediary.domain.Loaded
import pt.isel.sitediary.domain.LogValues
import pt.isel.sitediary.domain.Work
import pt.isel.sitediary.domain.getOrThrow
import pt.isel.sitediary.ui.common.LoadingScreen
import pt.isel.sitediary.ui.common.nav.BottomNavItem
import pt.isel.sitediary.ui.common.nav.BottomNavigationBar
import pt.isel.sitediary.ui.common.nav.TopBarWorkDetails
import pt.isel.sitediary.ui.theme.SiteDiaryPhoneTheme
import pt.isel.sitediary.ui.work.create.CreateLogScreen
import pt.isel.sitediary.ui.work.details.DetailsScreen
import pt.isel.sitediary.ui.work.log.LogsView

@Composable
fun WorkDetailsView(
    workState: LoadState<Work>,
    content: String,
    onBackRequested: () -> Unit = {},
    onLogSelected: (Int) -> Unit = {},
    onLogBackRequested: () -> Unit = {},
    onEditUploadRequested: () -> Unit = {},
    onUploadRequested: (String) -> Unit,
    onRemoveRequested: (String, String) -> Unit,
    onCreateClicked: (String) -> Unit,
    onDeleteSubmit: (Int, String) -> Unit,
    onEditSubmit: (String) -> Unit = {}
) {
    workState.let {
        if (it is Loaded && it.value.isSuccess) {
            val work = it.getOrThrow()
            SiteDiaryPhoneTheme {
                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopBarWorkDetails(title = work.name, work.verification)
                    },
                    bottomBar = {
                        BottomNavigationBar(
                            navController,
                            listOf(
                                BottomNavItem.Work.Details,
                                BottomNavItem.Work.Log,
                                BottomNavItem.Work.CreateLog
                            )
                        )
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination =
                            if (!work.files.isNullOrEmpty()) BottomNavItem.Work.CreateLog.route
                            else BottomNavItem.Work.Log.route
                    ) {
                        composable(BottomNavItem.Work.Details.route) {
                            DetailsScreen(work.toDetails(), innerPadding, onBackRequested)
                        }
                        composable(BottomNavItem.Work.Log.route) {
                            val logValues = LogValues(work.log, work.selectedLog)
                            LogsView(
                                logValues,
                                innerPadding,
                                onLogSelected,
                                onBackRequested,
                                onLogBackRequested,
                                onEditUploadRequested,
                                onDeleteSubmit,
                                onEditSubmit
                            )
                        }
                        composable(BottomNavItem.Work.CreateLog.route) {
                            CreateLogScreen(
                                files = work.files ?: mutableMapOf(),
                                content = content,
                                onCreateClicked = onCreateClicked,
                                onBackRequested = onBackRequested,
                                onRemoveRequested = onRemoveRequested,
                                onUploadRequested = onUploadRequested,
                                innerPadding = innerPadding
                            )
                        }
                    }
                }
            }
        } else {
            LoadingScreen()
        }
    }

}

