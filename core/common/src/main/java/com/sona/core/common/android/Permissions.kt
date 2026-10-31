package com.sona.core.common.android

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/** Returns true when [permission] is granted for this context. */
fun Context.isPermissionGranted(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
