package com.example.music.data.user

import com.google.gson.annotations.SerializedName


enum class SortMethodUser {
    @SerializedName("relevant")
    RELEVANT,

    @SerializedName("popular")
    POPULAR,


    @SerializedName("recent")
    RESENT
}