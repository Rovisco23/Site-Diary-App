package pt.isel.sitediary.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pt.isel.sitediary.R
import pt.isel.sitediary.ui.theme.SiteDiaryPhoneTheme

data class LoginModel(
    val username: String,
    val password: String
)

@Composable
fun LoginScreen(
    onLogin: (input: LoginModel) -> Unit = { }
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    SiteDiaryPhoneTheme {
        Scaffold(
            modifier = Modifier
                .fillMaxSize(),
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .background(Color(17, 17, 61))
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.mipmap.site_diary_icon),
                        contentDescription = "Site Diary Logo",
                        modifier = Modifier
                            .size(240.dp)
                            .clip(CircleShape)
                    )
                    Text(
                        text = "Site Diary",
                        color = Color(255, 122, 0),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.displayMedium,
                        fontFamily = FontFamily.Cursive,
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "Iniciar Sessão",
                        modifier = Modifier.padding(16.dp),
                        color = Color(255, 122, 0),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                    TextField(
                        singleLine = true,
                        label = { Text("Username ") },
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier
                            .width(232.dp)
                            .border(2.dp, Color.Black)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        singleLine = true,
                        label = { Text("Password") },
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier
                            .width(232.dp)
                            .border(2.dp, Color.Black),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                    Button(
                        colors = ButtonDefaults.buttonColors(Color(255, 122, 0)),
                        modifier = Modifier.width(120.dp),
                        onClick = {
                            onLogin(
                                LoginModel(
                                    username = username,
                                    password = password
                                )
                            )
                        }
                    ) {
                        Text(text = "Login")
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun LoginScreenPreview() {
    LoginScreen()
}