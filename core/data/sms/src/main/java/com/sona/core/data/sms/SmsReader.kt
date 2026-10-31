package com.sona.core.data.sms

import android.Manifest
import android.content.Context
import android.provider.Telephony
import com.sona.core.common.android.isPermissionGranted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class SmsMessage(
    val address: String?,
    val bodyPreview: String,
    val dateMillis: Long,
    val isRead: Boolean,
)

data class SmsInfo(
    val permissionGranted: Boolean,
    val count: Int,
    val messages: List<SmsMessage>,
)

interface SmsReader {
    suspend fun read(limit: Int = 100): SmsInfo
}

class AndroidSmsReader(private val context: Context) : SmsReader {

    override suspend fun read(limit: Int): SmsInfo = withContext(Dispatchers.IO) {
        if (!context.isPermissionGranted(Manifest.permission.READ_SMS)) {
            return@withContext SmsInfo(
                permissionGranted = false,
                count = 0,
                messages = emptyList(),
            )
        }

        val messages = mutableListOf<SmsMessage>()
        var count = 0

        runCatching {
            val projection = arrayOf(
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.READ,
            )
            context.contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                projection,
                null,
                null,
                "${Telephony.Sms.DATE} DESC",
            )?.use { cursor ->
                val addressIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                val bodyIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val dateIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
                val readIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.READ)
                while (cursor.moveToNext()) {
                    count++
                    if (messages.size < limit) {
                        messages.add(
                            SmsMessage(
                                address = cursor.getString(addressIndex),
                                bodyPreview = cursor.getString(bodyIndex).orEmpty().take(160),
                                dateMillis = cursor.getLong(dateIndex),
                                isRead = cursor.getInt(readIndex) != 0,
                            ),
                        )
                    }
                }
            }
        }

        SmsInfo(permissionGranted = true, count = count, messages = messages)
    }
}
