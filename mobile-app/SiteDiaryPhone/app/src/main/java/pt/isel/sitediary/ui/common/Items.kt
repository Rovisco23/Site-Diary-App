package pt.isel.sitediary.ui.common

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.isel.sitediary.domain.FileModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun ProfileItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(start = 24.dp, end = 16.dp, top = 8.dp)
    ) {
        Text(text = label, fontWeight = FontWeight.Bold)
        Text(text = value, textAlign = TextAlign.Center)
    }
}

@Composable
fun StatCard(number: String, label: String) {
    Card(
        modifier = Modifier.width(100.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = number,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)) {
        Text(text = label, fontWeight = FontWeight.Bold)
        Text(text = value)
    }
}


@Composable
fun CreateLogFileList(
    files: Map<String, File>,
    onRemoveRequested: (String) -> Unit
) {
    files.forEach {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                    contentDescription = "File Icon"
                )
                Text(
                    text = it.key,
                    fontSize = 20.sp
                )
            }
            IconButton(onClick = { onRemoveRequested(it.key) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete File Icon"
                )
            }
        }
    }
}

@Composable
fun LogFileList(files: List<FileModel>, editable: Boolean, onDeleteClicked: (Int) -> Unit) {
    files.forEach {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                    contentDescription = "File Icon"
                )
                Text(
                    text = it.fileName,
                    fontSize = 20.sp
                )
            }
            if (editable) IconButton(onClick = { onDeleteClicked(it.id) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete File Icon"
                )
            }
        }
    }
}

@Composable
fun ConfirmFileDeletion(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { },
        confirmButton = {
            OutlinedButton(
                border = BorderStroke(0.dp, Color.Unspecified),
                onClick = { onConfirm() }
            ) {
                Text(text = "Sim")
            }
        },
        dismissButton = {
            OutlinedButton(
                border = BorderStroke(0.dp, Color.Unspecified),
                onClick = { onDismiss() }
            ) {
                Text(text = "Cancelar")
            }

        },
        title = { Text(text = "Tem a certeza que quer eliminar o ficheiro?") },
        text = { Text(text = "Ficheiros eliminados não são recuperáveis") },
        icon = {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = "Warning"
            )
        }
    )
}

@SuppressLint("SimpleDateFormat")
fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm")
    return formatter.format(date)
}

@SuppressLint("SimpleDateFormat")
fun formatDateLog(date: Date): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy")
    val parts = formatter.format(date).split("/")
    return parts[0] + " de " + parts[1] + " de " + parts[2]
}