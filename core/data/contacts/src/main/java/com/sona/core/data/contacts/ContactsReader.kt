package com.sona.core.data.contacts

import android.Manifest
import android.content.Context
import android.provider.ContactsContract
import com.sona.core.common.android.isPermissionGranted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ContactEntry(
    val name: String,
    val phoneNumber: String?,
    val email: String?,
)

data class ContactsInfo(
    val permissionGranted: Boolean,
    val totalCount: Int,
    val contacts: List<ContactEntry>,
)

interface ContactsReader {
    suspend fun read(limit: Int = 200): ContactsInfo
}

class AndroidContactsReader(private val context: Context) : ContactsReader {

    override suspend fun read(limit: Int): ContactsInfo = withContext(Dispatchers.IO) {
        if (!context.isPermissionGranted(Manifest.permission.READ_CONTACTS)) {
            return@withContext ContactsInfo(
                permissionGranted = false,
                totalCount = 0,
                contacts = emptyList(),
            )
        }

        val entries = mutableListOf<ContactEntry>()
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
        )

        runCatching {
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC",
            )?.use { cursor ->
                val nameIndex = cursor.getColumnIndexOrThrow(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                )
                val numberIndex = cursor.getColumnIndexOrThrow(
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                )
                while (cursor.moveToNext() && entries.size < limit) {
                    entries.add(
                        ContactEntry(
                            name = cursor.getString(nameIndex).orEmpty(),
                            phoneNumber = cursor.getString(numberIndex),
                            email = null,
                        ),
                    )
                }
            }
        }

        ContactsInfo(
            permissionGranted = true,
            totalCount = entries.size,
            contacts = entries,
        )
    }
}
