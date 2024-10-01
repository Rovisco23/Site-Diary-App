package pt.isel.sitediary.ui.work.log

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import pt.isel.sitediary.R
import pt.isel.sitediary.domain.Author
import pt.isel.sitediary.domain.LogEntrySimplified
import pt.isel.sitediary.ui.common.formatDateLog
import pt.isel.sitediary.ui.common.nav.BottomNavItem
import pt.isel.sitediary.ui.common.nav.BottomNavigationBar
import pt.isel.sitediary.ui.common.nav.DefaultTopBar
import pt.isel.sitediary.ui.common.nav.TopBarGoBack
import pt.isel.sitediary.ui.theme.SiteDiaryPhoneTheme
import java.time.Instant
import java.util.Date

@Composable
fun LogsScreen(
    logs: List<LogEntrySimplified>,
    innerPadding: PaddingValues,
    onLogSelected: (logId: Int) -> Unit = {},
    onBackRequested: () -> Unit = {},
    needTopBar: Boolean = true
) {
    Column(modifier = Modifier.padding(innerPadding)) {
        if (needTopBar) TopBarGoBack(onBackRequested)
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            this.items(logs) {
                LogCard(it, onLogSelected)
            }
        }
    }
}

@Composable
fun LogCard(
    log: LogEntrySimplified,
    onLogSelected: (logId: Int) -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = Color(17, 17, 61)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = { onLogSelected(log.id) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.file_lines),
                    contentDescription = "Log Icon",
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatDateLog(log.createdAt),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Start,
                            color = Color.White,
                            maxLines = 1,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                        if (log.attachments) Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Attachments Icon",
                            tint = Color(255, 122, 0)
                        )
                        if (log.editable) Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editable Icon",
                            tint = Color(255, 122, 0)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Registado por " + log.author.name,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Start,
                            color = Color.White,
                            maxLines = 1,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun PreviewLogsScreen() {
    val date = Date(Instant.now().toEpochMilli())
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
            LogsScreen(
                logs = listOf(
                    LogEntrySimplified(
                        id = 1,
                        author = Author(
                            id = 1,
                            name = "Author Name",
                            role = "Role"
                        ),
                        createdAt = date,
                        editable = true,
                        attachments = true
                    ),
                    LogEntrySimplified(
                        id = 2,
                        author = Author(
                            id = 1,
                            name = "Author Name",
                            role = "Role"
                        ),
                        createdAt = date,
                        editable = false,
                        attachments = true
                    ),
                    LogEntrySimplified(
                        id = 3,
                        author = Author(
                            id = 1,
                            name = "Author Name",
                            role = "Role"
                        ),
                        createdAt = date,
                        editable = true,
                        attachments = false
                    ),
                    LogEntrySimplified(
                        id = 3,
                        author = Author(
                            id = 1,
                            name = "Author Name",
                            role = "Role"
                        ),
                        createdAt = date,
                        editable = false,
                        attachments = false
                    ),
                    LogEntrySimplified(
                        id = 4,
                        author = Author(
                            id = 1,
                            name = "Author Name",
                            role = "Role"
                        ),
                        createdAt = date,
                        editable = false,
                        attachments = false
                    ),
                    LogEntrySimplified(
                        id = 5,
                        author = Author(
                            id = 1,
                            name = "Author Name",
                            role = "Role"
                        ),
                        createdAt = date,
                        editable = false,
                        attachments = false
                    )
                ),
                innerPadding = innerPadding
            )
        }
    }
}