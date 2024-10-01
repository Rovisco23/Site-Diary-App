package pt.isel.sitediary.model

import pt.isel.sitediary.domainmodel.work.Author
import java.util.*

data class LogInputModel(
    val workId: UUID,
    val description: String
)

data class LogCredentialsModel(
    val logId: Int,
    val workId: UUID,
    val files: List<FileOutputModel>
) {
    fun filterFiles(): Pair<List<Int>, List<Int>> {
        val images = files.filter { f -> f.contentType == "Imagem" }.map { img -> img.id }
        val documents = files.filter { f -> f.contentType == "Documento" }.map { doc -> doc.id }
        return Pair(images, documents)
    }
}

data class LogOutputModel(
    val id: Int,
    val workId: UUID,
    val author: Author,
    val content: String,
    val editable: Boolean,
    val createdAt: Date,
    val modifiedAt: Date?,
    val files: List<FileOutputModel>
)

data class FileModel(val file: ByteArray, val fileName: String, val contentType: String)

fun List<FileModel>.filterFiles(): Pair<List<FileModel>, List<FileModel>> {
    val images = this.filter { f -> f.contentType.startsWith("image") }
    val documents = this.filter { f -> f.contentType.startsWith("application") }
    return Pair(images, documents)
}

data class FileOutputModel(
    val id: Int,
    val fileName: String,
    val contentType: String,
    val uploadDate: Date
)

data class DeleteFileModel(
    val logId: Int,
    val fileId: Int,
    val type: String
)