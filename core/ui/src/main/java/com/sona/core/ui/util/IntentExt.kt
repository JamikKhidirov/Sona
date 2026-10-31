package com.sona.core.ui.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri

/** Opens the system share sheet with plain text. */
fun Context.shareText(text: String, title: String = "Share via") {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    val chooser = Intent.createChooser(send, title)
    if (this !is Activity) {
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(chooser)
}

/** Opens an http(s) link in a browser or the matching Audius app. */
fun Context.openUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    if (this !is Activity) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(intent)
}

/** Shares a track title, artist and public link. */
fun Context.shareTrack(title: String, artist: String, url: String) {
    shareText("$title — $artist\n$url", title = "Share track")
}
