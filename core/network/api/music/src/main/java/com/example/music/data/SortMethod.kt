package com.example.music.data
import com.google.gson.annotations.SerializedName


enum class SortMethod {
    @SerializedName("title")
    TITLE,

    @SerializedName("artist_name")
    ARTIST_NAME,

    @SerializedName("release_date")
    RELEASE_DATE,

    @SerializedName("last_listen_date")
    LAST_LISTEN_DATA,

    @SerializedName("added_date")
    ADDED_DATE,


    @SerializedName("plays")
    PLAYS,

    @SerializedName("reposts")
    REPORTS,

    @SerializedName("saves")
    SAVES,

    @SerializedName("most_listens_by_user")
    MOST_LISTEN_BY_USER
}