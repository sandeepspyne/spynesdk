package com.spyneai.model.marketupdate

import com.google.gson.annotations.SerializedName
import com.spyneai.model.channel.ChannelUpdateRequest

data class ShootMarketUpdateRequest(
     @SerializedName("shootId")
     val shootId: String,
     @SerializedName("marketPlace")
     val marketPlace: ArrayList<ChannelUpdateRequest>
)
