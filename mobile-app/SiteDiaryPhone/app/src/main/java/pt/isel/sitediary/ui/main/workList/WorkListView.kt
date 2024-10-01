package pt.isel.sitediary.ui.main.workList

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.unit.dp
import pt.isel.sitediary.domain.LoadState
import pt.isel.sitediary.domain.Loaded
import pt.isel.sitediary.domain.Loading
import pt.isel.sitediary.domain.MainValues
import pt.isel.sitediary.ui.common.LoadingScreen
import java.util.UUID

@Composable
fun WorkListView(
    workList: LoadState<MainValues>,
    onRefresh: () -> Unit,
    onWorkSelected: (workId: UUID) -> Unit,
    innerPadding: PaddingValues
) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        Box(
            modifier = Modifier,
            contentAlignment = Alignment.TopCenter
        ) {
            Column {
                var searchText by remember { mutableStateOf("") }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .weight(1f)
                            .border(2.dp, Color(17, 17, 61)),
                        placeholder = { Text("Procure por o nome da Obra") }
                    )
                    IconButton(
                        onClick = { onRefresh() },
                        modifier = Modifier
                            .size(48.dp)
                            .border(2.dp, Color(17, 17, 61), CircleShape)
                            .background(Color(17, 17, 61), CircleShape),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                workList.let {
                    if (it is Loaded && it.value.isSuccess) {
                        it.value.getOrNull()?.let { values ->
                            WorkListScreen(values.workList, searchText, onWorkSelected)
                        }
                    } else if (it is Loading) {
                        LoadingScreen()
                    }
                }
            }
        }
    }
}