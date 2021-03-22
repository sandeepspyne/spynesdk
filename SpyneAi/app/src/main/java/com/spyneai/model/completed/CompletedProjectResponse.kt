package com.spyneai.model.projects

import com.google.gson.annotations.SerializedName
import com.spyneai.model.dashboard.Payload
import com.spyneai.model.login.Header
import com.spyneai.model.login.MsgInfo

data class CompletedProjectResponse(
    @SerializedName("output_image_url")
    val output_image_url: String,
    @SerializedName("sku_id")
    val sku_id: String,
    @SerializedName("sku_name")
    val sku_name: String,
    @SerializedName("created_at")
    val created_at: String,

)
