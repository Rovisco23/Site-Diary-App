package pt.isel.sitediary.ui.main.profile

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import pt.isel.sitediary.domain.LoadState
import pt.isel.sitediary.domain.Loaded
import pt.isel.sitediary.domain.MainValues
import pt.isel.sitediary.ui.common.LoadingScreen

@Composable
fun ProfileView(
    profile: LoadState<MainValues>,
    changeProfilePicture: () -> Unit,
    onLogoutRequest: () -> Unit,
    innerPadding: PaddingValues
) {
    profile.let {
        if (it is Loaded && it.value.isSuccess) {
            it.value.getOrNull()?.let { values ->
                ProfileScreen(values.profile, changeProfilePicture, onLogoutRequest, innerPadding)
            }
        } else {
            LoadingScreen()
        }
    }
}
