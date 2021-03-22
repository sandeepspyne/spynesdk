package com.spyneai.model.marketupdate

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.spyneai.model.channel.ChannelUpdateRequest
import com.spyneai.model.channel.ChannelsResponse
import com.spyneai.model.marketupdate.MarketPlace

data class ShootMarketUpdateRequest(
     @SerializedName("shootId")
     val shootId: String,
     @SerializedName("marketPlace")
     val marketPlace: ArrayList<ChannelUpdateRequest>
)
