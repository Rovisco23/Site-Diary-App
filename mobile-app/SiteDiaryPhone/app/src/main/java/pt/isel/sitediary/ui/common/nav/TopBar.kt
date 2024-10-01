package pt.isel.sitediary.ui.common.nav


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.isel.sitediary.R
import pt.isel.sitediary.ui.theme.SiteDiaryPhoneTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultTopBar() {
    TopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.mipmap.site_diary_icon),
                        contentDescription = "Icon"
                    )
                    Text(
                        text = "Site Diary",
                        color = Color(255, 122, 0),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(17, 17, 61)
        )
    )
}

@Composable
fun TopBarGoBack(onBackRequested: () -> Unit = { }) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(
            onClick = onBackRequested,
            modifier = Modifier
                .padding(start = 0.dp)
                .size(32.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(id = R.string.top_bar_go_back),
                tint = Color(17, 17, 61),
                modifier = Modifier
                    .padding(start = 0.dp)
                    .size(32.dp)
            )
        }
        Text(
            text = "Voltar",
            color = Color(17, 17, 61),
            fontSize = 26.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun TopBarLogGoBack(
    onBackRequested: () -> Unit = { },
    navigateTo: () -> Unit = {},
    buttonText: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.weight(1f)

        ) {
            IconButton(
                onClick = onBackRequested,
                modifier = Modifier
                    .padding(start = 0.dp, top = 0.dp)
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.top_bar_go_back),
                    tint = Color(17, 17, 61),
                    modifier = Modifier
                        .padding(start = 0.dp)
                        .size(32.dp)
                )
            }
            Text(
                text = "Voltar",
                color = Color(17, 17, 61),
                fontSize = 26.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Button(
            onClick = navigateTo,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(17, 17, 61),
                contentColor = Color.White //Color(255, 122, 0)
            ),
            border = BorderStroke(2.dp, Color(255, 122, 0)),
            shape = RectangleShape,
            modifier = Modifier
                .padding(2.dp)
                .width(150.dp)
                .height(48.dp)
        ) {
            Text(
                text = buttonText,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarWorkDetails(title: String, verification: Boolean) {
    TopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title,
                        color = Color(255, 122, 0),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    if (verification) Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verification Icon",
                        tint = Color.Green
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(17, 17, 61)
        )
    )
}

@Preview
@Composable
private fun TopBarPreview() {
    SiteDiaryPhoneTheme {
        Column {
            DefaultTopBar()
            TopBarWorkDetails("Casa da D. Maria", true)
            TopBarGoBack()
        }
    }
}