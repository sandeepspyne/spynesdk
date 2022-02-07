package com.spyneai.orders.data.response

import com.google.gson.annotations.SerializedName
import com.spyneai.shoot.repository.model.image.Image

data class ImagesOfSkuRes(
    val `data`: List<Image>,
    val message: String,
    val paid: String,
    val sku_status: String,
    val staus: Int,
    var fromLocal: Boolean = true
)