package com.ftsdoc.mobile.data.local

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.provider.OpenableColumns
import com.ftsdoc.mobile.domain.model.PickedFile
import com.ftsdoc.mobile.domain.model.UserMessageException
import java.util.Locale

class DocumentFileReader(private val context: Context) {
    fun inspect(uri: Uri): PickedFile {
        val resolver = context.contentResolver
        val displayName = queryDisplayName(uri) ?: uri.lastPathSegment ?: "file"
        val mime = resolver.getType(uri) ?: guessMime(displayName)
        val size = querySize(uri)
        if (size != null && size > MAX_FILE_BYTES) {
            throw UserMessageException("Файл «$displayName» больше 15 МБ")
        }
        return PickedFile(
            uri = uri.toString(),
            displayName = displayName,
            mimeType = mime,
            pageCount = pageCount(uri, mime, displayName),
        )
    }

    fun readBytes(uriString: String): ByteArray {
        val uri = Uri.parse(uriString)
        val bytes = context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.readBytes()
        } ?: throw UserMessageException("Не удалось прочитать файл")
        if (bytes.isEmpty()) {
            throw UserMessageException("Файл пустой")
        }
        if (bytes.size > MAX_FILE_BYTES) {
            throw UserMessageException("Файл больше 15 МБ")
        }
        return bytes
    }

    private fun pageCount(uri: Uri, mime: String, displayName: String): Int {
        if (mime != "application/pdf") return 1
        return try {
            val count = context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                PdfRenderer(descriptor).use { renderer -> renderer.pageCount }
            } ?: throw UserMessageException("Не удалось открыть «$displayName»")
            count.coerceAtLeast(1)
        } catch (error: UserMessageException) {
            throw error
        } catch (_: Exception) {
            throw UserMessageException("Не удалось прочитать PDF «$displayName»")
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        val cursor = context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        ) ?: return null
        return cursor.use {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && index >= 0) it.getString(index) else null
        }
    }

    private fun querySize(uri: Uri): Long? {
        val cursor = context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.SIZE),
            null,
            null,
            null,
        ) ?: return null
        return cursor.use {
            val index = it.getColumnIndex(OpenableColumns.SIZE)
            if (it.moveToFirst() && index >= 0 && !it.isNull(index)) it.getLong(index) else null
        }
    }

    private fun guessMime(name: String): String {
        return when (name.substringAfterLast('.', "").lowercase(Locale.US)) {
            "pdf" -> "application/pdf"
            "png" -> "image/png"
            "webp" -> "image/webp"
            "gif" -> "image/gif"
            "jpg", "jpeg" -> "image/jpeg"
            else -> "application/octet-stream"
        }
    }

    private companion object {
        const val MAX_FILE_BYTES = 15 * 1024 * 1024
    }
}
