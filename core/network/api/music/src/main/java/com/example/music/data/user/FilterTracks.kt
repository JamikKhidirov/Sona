package com.example.music.data.user

import com.google.gson.annotations.SerializedName


enum class FilterTracks {
    @SerializedName("all")
    ALL,
    @SerializedName("public")
    PUBLIC,
    @SerializedName("unlisted")
    UNLISTED
}
