package com.sona.core.data.nfc

import android.content.Context
import android.nfc.NfcManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class NfcInfo(
    val supported: Boolean,
    val enabled: Boolean,
)

interface NfcReader {
    suspend fun read(): NfcInfo
}

class AndroidNfcReader(private val context: Context) : NfcReader {

    override suspend fun read(): NfcInfo = withContext(Dispatchers.IO) {
        val adapter = context.getSystemService(NfcManager::class.java)?.defaultAdapter
        NfcInfo(
            supported = adapter != null,
            enabled = runCatching { adapter?.isEnabled == true }.getOrDefault(false),
        )
    }
}
