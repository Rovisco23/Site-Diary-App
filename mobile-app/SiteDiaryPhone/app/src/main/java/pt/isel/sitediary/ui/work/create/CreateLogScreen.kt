package pt.isel.sitediary.ui.work.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.isel.sitediary.ui.common.CreateLogFileList
import pt.isel.sitediary.ui.common.nav.TopBarGoBack
import java.io.File

@Composable
fun CreateLogScreen(
    files: Map<String, File>,
    content: String,
    onCreateClicked: (String) -> Unit,
    onBackRequested: () -> Unit,
    onRemoveRequested: (String, String) -> Unit,
    onUploadRequested: (String) -> Unit,
    innerPadding: PaddingValues
) {
    var txt by remember { mutableStateOf(content) }
    Column(
        modifier = Modifier
            .padding(innerPadding)
    ) {
        TopBarGoBack(onBackRequested)
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Observações",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            TextField(
                value = txt,
                onValueChange = { txt = it },
                modifier = Modifier.fillMaxWidth()
            )
            HorizontalDivider(
                modifier = Modifier
                    .width(400.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 4.dp, top = 4.dp),
                thickness = 2.dp,
                color = Color(17, 17, 61)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ficheiros",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = { onUploadRequested(txt) }) {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = "Upload File Icon"
                    )
                }
            }
            CreateLogFileList(
                files = files,
                onRemoveRequested = { onRemoveRequested(txt, it) }
            )
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    colors = ButtonDefaults.buttonColors(Color(255, 122, 0)),
                    enabled = txt.isNotBlank(),
                    modifier = Modifier.width(180.dp),
                    onClick = {
                        onCreateClicked(txt)
                    }
                ) {
                    Text(text = "Registar Observação")
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewCreateLogScreen() {
    CreateLogScreen(
        files = emptyMap(),
        content = "",
        onCreateClicked = {},
        onBackRequested = {},
        onRemoveRequested = { s, s1 -> },
        onUploadRequested = {},
        innerPadding = PaddingValues()
    )
}