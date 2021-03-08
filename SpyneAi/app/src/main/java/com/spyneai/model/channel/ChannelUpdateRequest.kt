package com.spyneai.model.channel

data class ChannelUpdateRequest (
        var markId : String,
        var displayThumbnail : String,
        val displayName : String,
)