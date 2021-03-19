package com.spyneai.model.ai
import com.google.gson.annotations.SerializedName

data class WaterMarkResponse(
    @SerializedName("status")
    val status : Int,
    @SerializedName("message")
    val message :  String,
    @SerializedName("output_url")
    val output_url : String,
    @SerializedName("input_url")
    val input_url : String
)
