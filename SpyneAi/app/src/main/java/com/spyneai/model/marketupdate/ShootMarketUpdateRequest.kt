package com.spyneai.model.marketupdate

import com.google.gson.annotations.Expose
import com.spyneai.model.channel.ChannelUpdateRequest
import com.spyneai.model.channel.ChannelsResponse
import com.spyneai.model.marketupdate.MarketPlace

data class ShootMarketUpdateRequest(
     val shootId: String,
     val marketPlace: ArrayList<ChannelUpdateRequest>
)
