package pt.isel.sitediary.ui.work.log

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.navigation.compose.rememberNavController
import pt.isel.sitediary.domain.Author
import pt.isel.sitediary.domain.FileModel
import pt.isel.sitediary.domain.LogEntry
import pt.isel.sitediary.ui.common.ConfirmFileDeletion
import pt.isel.sitediary.ui.common.LogFileList
import pt.isel.sitediary.ui.common.formatDate
import pt.isel.sitediary.ui.common.nav.BottomNavItem
import pt.isel.sitediary.ui.common.nav.BottomNavigationBar
import pt.isel.sitediary.ui.common.nav.DefaultTopBar
import pt.isel.sitediary.ui.common.nav.TopBarGoBack
import pt.isel.sitediary.ui.theme.SiteDiaryPhoneTheme
import java.time.Instant
import java.util.Date
import java.util.UUID

@Composable
fun LogScreen(
    log: LogEntry,
    innerPadding: PaddingValues,
    onBackRequested: () -> Unit = {},
    onUploadRequested: () -> Unit = {},
    onDeleteSubmit: (Int, String) -> Unit,
    onEditSubmit: (String) -> Unit = {},
) {
    var editing by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(0) }
    var content by remember { mutableStateOf(log.content) }
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
                text = "Autor: ${log.author.name}",
                fontSize = 16.sp
            )
            Text(
                text = "Data de Criação: " + formatDate(log.createdAt),
                fontSize = 16.sp
            )
            Text(
                text = "Data de Alteração: " + formatDate(log.modifiedAt),
                fontSize = 16.sp
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Observações",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (log.editable && !editing) IconButton(onClick = { editing = true }) {
                    Icon(imageVector = Icons.Default.EditNote, contentDescription = "Editing Icon")
                }
                if (log.editable && editing) {
                    IconButton(onClick = {
                        editing = false
                        if (content.isNotEmpty()) onEditSubmit(content) else content = log.content
                    }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Confirm Edit Icon"
                        )
                    }
                    IconButton(onClick = { editing = false; content = log.content }) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = "Cancel Edit Icon"
                        )
                    }
                }
            }
            if (!editing) Text(
                text = content,
                fontSize = 20.sp
            )
            else TextField(
                value = content,
                onValueChange = { content = it },
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
                if (log.editable) IconButton(onClick = { onUploadRequested() }) {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = "Upload File Icon"
                    )
                }
            }
            LogFileList(log.files, log.editable) {
                deleting = true
                selected = it
            }
            if (deleting) ConfirmFileDeletion(
                onConfirm = {
                    deleting = false
                    val file = log.files.find { it.id == selected }!!
                    onDeleteSubmit(file.id, file.contentType)
                    selected = 0
                },
                onDismiss = { deleting = false }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun PreviewLogScreen() {
    val date = Date(Instant.now().toEpochMilli())
    var log by remember {
        mutableStateOf(
            LogEntry(
                id = 1,
                workId = UUID.randomUUID(),
                author = Author(
                    id = 1,
                    name = "JaneDoe1234",
                    role = "Manager"
                ),
                content = "A montagem do andaime foi realizada fora dos parâmetros estabelecidos, revelando uma falta de precisão e atenção aos detalhes. A equipe de construção demonstrou deficiências técnicas ao lidar com desafios específicos, resultando em um trabalho de qualidade insatisfatória que não atende às expectativas do cliente. A comunicação ineficaz e a falta de coordenação entre todos os envolvidos contribuíram significativamente para as falhas observadas nesta fase do projeto.",
                editable = true,
                createdAt = date,
                modifiedAt = date,
                files = listOf(
                    FileModel(1, "file1", "Image"),
                    FileModel(2, "file2", "Image"),
                    FileModel(3, "file3", "Image"),
                    FileModel(4, "file4", "Image"),
                    FileModel(5, "file5", "Image"),
                    FileModel(6, "file6", "Image")
                )
            )
        )
    }
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
                        BottomNavItem.Work.Details,
                        BottomNavItem.Work.Log,
                        BottomNavItem.Work.CreateLog
                    )
                )
            }
        ) { innerPadding ->
            LogScreen(
                log,
                innerPadding,
                onEditSubmit = { log = log.copy(content = it) },
                onDeleteSubmit = { fileId, _ ->
                    log = log.copy(files = log.files.filter { it.id != fileId })
                }
            )
        }
    }
}