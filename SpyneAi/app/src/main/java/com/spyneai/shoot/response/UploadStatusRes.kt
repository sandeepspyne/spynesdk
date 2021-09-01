package com.spyneai.shoot.response

import com.google.gson.annotations.SerializedName

class UploadStatusRes(
    @SerializedName("message") val message : String,
    @SerializedName("status") val status : Int,
    @SerializedName("data") val data: Data
){
    data class Data(
        @SerializedName("upload")
        val upload: Boolean,
        @SerializedName("project_id")
        val projectId: String,
        @SerializedName("sku_id")
        val skuId: String,
        @SerializedName("sequence")
        val sequence: Int,
        @SerializedName("image_category")
        val imageCategory: String
    )
}