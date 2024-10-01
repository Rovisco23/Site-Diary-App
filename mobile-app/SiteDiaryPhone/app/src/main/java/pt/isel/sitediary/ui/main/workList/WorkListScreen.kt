package pt.isel.sitediary.ui.main.workList

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pt.isel.sitediary.R
import pt.isel.sitediary.domain.Address
import pt.isel.sitediary.domain.Location
import pt.isel.sitediary.domain.WorkSimplified
import pt.isel.sitediary.domain.WorkState
import java.util.UUID

@Composable
fun WorkListScreen(
    workList: List<WorkSimplified>,
    searchText: String,
    onWorkSelected: (workId: UUID) -> Unit
) {
    val filteredWorks = workList.filter { work ->
        work.name.contains(searchText, ignoreCase = true)
    }
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 10.dp)
    ) {
        this.items(filteredWorks) {
            WorkCard(it, onWorkSelected)
        }
    }
}

@Composable
fun WorkCard(
    work: WorkSimplified,
    onWorkSelected: (workId: UUID) -> Unit
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
        onClick = { onWorkSelected(work.id) }
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
                    painter = painterResource(id = R.mipmap.work_icon),
                    contentDescription = "Work Image",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = work.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                textAlign = TextAlign.Start,
                                color = Color(255, 122, 0),
                                modifier = Modifier
                            )
                            if (work.verification) Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verification Icon",
                                tint = Color.Green
                            )
                        }
                        Text(
                            text = work.state.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(255, 122, 0),
                            maxLines = 1,
                            textAlign = TextAlign.End,
                            modifier = Modifier
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Owner",
                            modifier = Modifier.size(24.dp),
                            tint = Color.White
                        )
                        Text(
                            text = work.owner,
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Start,
                            color = Color.White,
                            maxLines = 1,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Row {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = "Location Pin",
                            modifier = Modifier.size(24.dp),
                            tint = Color.White
                        )
                        Column {
                            Text(
                                text = work.address.street,
                                style = MaterialTheme.typography.labelMedium,
                                textAlign = TextAlign.Start,
                                color = Color.White,
                                maxLines = 1,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "${work.address.postalCode}, ${work.address.location.parish}",
                                style = MaterialTheme.typography.labelMedium,
                                textAlign = TextAlign.Start,
                                color = Color.White,
                                maxLines = 1,
                                modifier = Modifier
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewWorkListScreen() {
    WorkListScreen(
        workList = listOf(
            WorkSimplified(
                id = UUID.randomUUID(),
                name = "Work Name",
                state = WorkState.IN_PROGRESS,
                owner = "João Mota",
                address = Address(
                    location = Location(
                        district = "Lisboa",
                        county = "Lisboa",
                        parish = "Marvila"
                    ),
                    street = "Rua xpto",
                    postalCode = "1234-567",
                ),
                verification = true
            )
        ),
        searchText = "",
        onWorkSelected = {}
    )
}