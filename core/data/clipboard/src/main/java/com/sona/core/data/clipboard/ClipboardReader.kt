package com.sona.core.data.clipboard

import android.content.ClipboardManager
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ClipboardInfo(
    val hasText: Boolean,
    val text: String?,
    val textLength: Int,
)

interface ClipboardReader {
    suspend fun read(): ClipboardInfo
}

class AndroidClipboardReader(private val context: Context) : ClipboardReader {

    override suspend fun read(): ClipboardInfo = withContext(Dispatchers.IO) {
        val manager = context.getSystemService(ClipboardManager::class.java)
        val clip = runCatching { manager?.primaryClip }.getOrNull()
        val text = runCatching {
            if (clip == null || clip.itemCount == 0) {
                null
            } else {
                clip.getItemAt(0).coerceToText(context)?.toString()
            }
        }.getOrNull()

        ClipboardInfo(
            hasText = text != null,
            text = text,
            textLength = text?.length ?: 0,
        )
    }
}
