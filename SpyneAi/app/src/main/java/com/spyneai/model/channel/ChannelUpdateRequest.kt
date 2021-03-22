package com.spyneai.model.channel

import com.google.gson.annotations.SerializedName

data class ChannelUpdateRequest (
        @SerializedName("markId")
        var markId : String,
        @SerializedName("displayThumbnail")
        var displayThumbnail : String,
        @SerializedName("displayName")
        val displayName : String,
)