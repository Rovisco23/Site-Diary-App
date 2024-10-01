package pt.isel.sitediary.ui.work.members

import android.content.ContentValues
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pt.isel.sitediary.R
import pt.isel.sitediary.domain.Association
import pt.isel.sitediary.domain.Member
import pt.isel.sitediary.domain.Technician
import pt.isel.sitediary.ui.theme.SiteDiaryPhoneTheme

@Composable
fun MembersScreen(
    members: List<Member>,
    technicians: List<Technician>,
    innerPadding: PaddingValues,
    onMemberSelected: (Int) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(innerPadding)
    ) {
        this.items(members) {
            MemberCard(it, onMemberSelected)
        }
    }
}

@Composable
fun MemberCard(
    member: Member,
    onMemberSelected: (Int) -> Unit
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
        onClick = { onMemberSelected(member.id) }
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
                    painter = painterResource(R.drawable.ic_default_profile),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.onPrimary, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    textAlign = TextAlign.Start,
                    color = Color(255, 122, 0),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = member.role,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(255, 122, 0),
                    maxLines = 1,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewMembersScreen() {
    val members = listOf(
        Member(1, "Jane Doe", "Manager"),
        Member(2, "John Doe", "Worker")
    )
    val technicians = listOf(
        Technician("Jane Doe", "Manager", Association("Work", 1)),
        Technician("John Doe", "Worker", Association("Work", 2))
    )
    SiteDiaryPhoneTheme {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
        ) { innerPadding ->
            MembersScreen(members = members, technicians = technicians, innerPadding = innerPadding) { userId ->
                Log.v(ContentValues.TAG, "Member selected: $userId")
            }
        }
    }
}