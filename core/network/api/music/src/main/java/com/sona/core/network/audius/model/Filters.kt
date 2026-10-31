package com.sona.core.network.audius.model

import com.google.gson.annotations.SerializedName

/** Filter used by the `users/{id}/tracks` endpoint. */
enum class FilterTracks {
    @SerializedName("all")
    ALL,

    @SerializedName("public")
    PUBLIC,

    @SerializedName("unlisted")
    UNLISTED,
}

/** Filter used by the AI attributed tracks endpoint. */
enum class FilterTracksLimited {
    @SerializedName("all")
    ALL,

    @SerializedName("public")
    PUBLIC,
}

/** Time window for trend endpoints. */
enum class TimePeriod {
    @SerializedName("week")
    WEEK,

    @SerializedName("month")
    MONTH,

    @SerializedName("year")
    YEAR,

    @SerializedName("allTime")
    ALL_TIME,
}

/** Sort option for AI attributed tracks. */
enum class SortAi {
    @SerializedName("date")
    DATE,

    @SerializedName("plays")
    PLAYS,
}

enum class SortDirection {
    @SerializedName("asc")
    ASC,

    @SerializedName("desc")
    DESC,
}

enum class SortMethod {
    @SerializedName("title")
    TITLE,

    @SerializedName("artist_name")
    ARTIST_NAME,

    @SerializedName("release_date")
    RELEASE_DATE,

    @SerializedName("last_listen_date")
    LAST_LISTEN_DATE,

    @SerializedName("added_date")
    ADDED_DATE,

    @SerializedName("plays")
    PLAYS,

    @SerializedName("reposts")
    REPOSTS,

    @SerializedName("saves")
    SAVES,

    @SerializedName("most_listens_by_user")
    MOST_LISTENS_BY_USER,
}

enum class SortMethodUser {
    @SerializedName("relevant")
    RELEVANT,

    @SerializedName("popular")
    POPULAR,

    @SerializedName("recent")
    RECENT,
}
