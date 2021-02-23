package com.spyneai.model.channel

data class ChannelsResponse (
        var market_id : String,
        var image_url : String,
        val image_credit : Int,
        var category : String,
        var hex_code : String
)