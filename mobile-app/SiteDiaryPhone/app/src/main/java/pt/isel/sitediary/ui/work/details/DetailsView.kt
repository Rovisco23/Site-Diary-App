package pt.isel.sitediary.ui.work.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import pt.isel.sitediary.domain.LoadState
import pt.isel.sitediary.domain.Loaded
import pt.isel.sitediary.domain.Work
import pt.isel.sitediary.domain.WorkDetails
import pt.isel.sitediary.ui.common.LoadingScreen
import pt.isel.sitediary.ui.common.nav.TopBarGoBack

@Composable
fun DetailsView(
    details: WorkDetails,
    innerPadding: PaddingValues,
    onBackRequested: () -> Unit = {}
) {
                DetailsScreen(details, innerPadding, onBackRequested)

}