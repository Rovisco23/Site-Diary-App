package pt.isel.sitediary.ui.work.members

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import pt.isel.sitediary.R
import pt.isel.sitediary.domain.Association
import pt.isel.sitediary.domain.Location
import pt.isel.sitediary.domain.Profile
import pt.isel.sitediary.domain.User
import pt.isel.sitediary.ui.common.ProfileItem
import pt.isel.sitediary.ui.common.nav.BottomNavItem
import pt.isel.sitediary.ui.common.nav.BottomNavigationBar
import pt.isel.sitediary.ui.common.nav.TopBarGoBack
import pt.isel.sitediary.ui.common.nav.DefaultTopBar
import pt.isel.sitediary.ui.theme.SiteDiaryPhoneTheme

@Composable
fun MemberProfileScreen(
    profile: Profile,
    innerPadding: PaddingValues,
    onBackRequested: () -> Unit = {}
) {
    val imageModifier = Modifier
        .width(200.dp)
        .height(200.dp)
        .clip(CircleShape)
        .padding(16.dp)
        .border(2.dp, Color(17, 17, 61), CircleShape)
    Column(modifier = Modifier.padding(innerPadding)) {
        TopBarGoBack(onBackRequested)
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (profile.profilePicture != null) {
                        Image(
                            bitmap = profile.profilePicture.asImageBitmap(),
                            contentDescription = "Profile Picture",
                            modifier = imageModifier
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.ic_default_profile),
                            contentDescription = "Profile Picture",
                            modifier = imageModifier
                        )
                    }
                    Column {
                        ProfileItem(label = "Nome", value = profile.user.getName())
                        ProfileItem(label = "NIF", value = profile.user.nif.toString())
                        ProfileItem(label = "Tipo de Conta", value = profile.user.role)
                    }
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier.width(350.dp).align(Alignment.CenterHorizontally),
            thickness = 2.dp,
            color = Color(17, 17, 61)
        )
        ProfileItem(label = "Username", value = profile.user.username)
        ProfileItem(label = "Email", value = profile.user.email)
        ProfileItem(label = "Número de Telemóvel", value = profile.user.getPhoneNumber())
        ProfileItem(label = "Associação", value = profile.user.association.toString())
        ProfileItem(label = "Localização", value = profile.user.location.toString())
    }
}

@Preview
@Composable
fun PreviewMemberProfileScreen() {
    SiteDiaryPhoneTheme {
        val navController = rememberNavController()
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = { DefaultTopBar() },
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
            MemberProfileScreen(
                Profile(
                    user = User(
                        id = 1,
                        username = "JaneDoe1234",
                        firstName = "Jane",
                        lastName = "Doe",
                        email = "jane@gmail.com",
                        nif = 123456789,
                        phone = "912345678",
                        role = "Manager",
                        association = Association("Work", 1),
                        location = Location("Lisboa", "Lisboa", "Marvila")
                    ),
                    profilePicture = null
                ),
                innerPadding
            ) {}
        }
    }
}