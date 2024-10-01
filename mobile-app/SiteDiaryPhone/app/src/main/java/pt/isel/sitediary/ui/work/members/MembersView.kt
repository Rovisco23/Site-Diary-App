package pt.isel.sitediary.ui.work.members

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import pt.isel.sitediary.domain.LoadState
import pt.isel.sitediary.domain.Loaded
import pt.isel.sitediary.domain.Work
import pt.isel.sitediary.ui.common.LoadingScreen

@Composable
fun MembersView(
    work: LoadState<Work>,
    innerPadding: PaddingValues,
    onMemberSelected: (Int) -> Unit = {},
    onBackRequested: () -> Unit = {},
    onProfileBackRequested: () -> Unit = {}
) {
    work.let {
        if (it is Loaded && it.value.isSuccess) {
            it.value.getOrNull()?.let { work ->
                val selectedMember = work.selectedMember
                if (selectedMember != null) {
                    MemberProfileScreen(selectedMember, innerPadding, onProfileBackRequested)
                } else {
                    val members = work.members
                    MembersScreen(members, emptyList(), innerPadding, onMemberSelected)
                }
            }
        } else {
            LoadingScreen()
        }
    }
}

