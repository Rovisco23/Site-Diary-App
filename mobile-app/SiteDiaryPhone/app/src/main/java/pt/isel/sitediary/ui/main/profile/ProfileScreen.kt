package pt.isel.sitediary.ui.main.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
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
import pt.isel.sitediary.ui.common.nav.DefaultTopBar
import pt.isel.sitediary.ui.theme.SiteDiaryPhoneTheme

@Composable
fun ProfileScreen(
    profile: Profile,
    changeProfilePicture: () -> Unit,
    onLogoutRequest: () -> Unit,
    innerPadding: PaddingValues
) {
    val imageModifier = Modifier
        .size(180.dp)
        .clip(CircleShape)
        .border(2.dp, Color(17, 17, 61), CircleShape)
        .clickable { changeProfilePicture() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(innerPadding)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box (
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(180.dp)
                            .clip(CircleShape)
                            .padding(16.dp)
                    ) {
                        if (profile.profilePicture != null) {
                            Image(
                                bitmap = profile.profilePicture.asImageBitmap(),
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = imageModifier
                            )
                        } else {
                            Image(
                                painter = painterResource(R.drawable.ic_default_profile),
                                contentDescription = "Profile Picture",
                                modifier = imageModifier
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ProfileItem(label = "Nome", value = profile.user.getName())
                        ProfileItem(label = "NIF", value = profile.user.nif.toString())
                        ProfileItem(label = "Tipo de Conta", value = profile.user.role)
                    }
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier
                .width(350.dp)
                .align(Alignment.CenterHorizontally),
            thickness = 2.dp,
            color = Color(17, 17, 61)
        )
        ProfileItem(label = "Username", value = profile.user.username)
        ProfileItem(label = "Email", value = profile.user.email)
        ProfileItem(label = "Número de Telemóvel", value = profile.user.getPhoneNumber())
        ProfileItem(label = "Associação", value = profile.user.association.toString())
        ProfileItem(label = "Localização", value = profile.user.location.toString())
        Button(
            onClick = onLogoutRequest,
            colors = ButtonDefaults.buttonColors(containerColor = Color(17, 17, 61))
        ) {
            Text(text = "Logout")
        }
    }
}

@Preview
@Composable
fun PreviewProfileScreen() {
    SiteDiaryPhoneTheme {
        val navController = rememberNavController()
        Scaffold(
            modifier = Modifier
                .fillMaxSize(),
            topBar = {
                DefaultTopBar()
            },
            bottomBar = {
                BottomNavigationBar(
                    navController,
                    listOf(
                        BottomNavItem.Main.Home,
                        BottomNavItem.Main.UserProfile
                    )
                )
            }
        ) { innerPadding ->
            ProfileScreen(
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
                {},
                {},
                innerPadding
            )
        }
    }
}