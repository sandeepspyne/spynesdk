package com.spyneai.draft.data


import com.google.gson.annotations.SerializedName
import com.spyneai.shoot.repository.model.sku.Sku

data class PagedSkuRes(
    @SerializedName("data")
    val `data`: ArrayList<Sku>,
    @SerializedName("message")
    val message: String
)