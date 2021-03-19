package com.spyneai.model.uploadRough

import com.google.gson.annotations.SerializedName

data class UploadPhotoRequest (
        @SerializedName("skuName")
        val skuName : String,
        @SerializedName("skuId")
        val skuId : String,
        @SerializedName("type")
        val type : String,
        @SerializedName("indexNumber")
        val indexNumber : Int,
        @SerializedName("shootId")
        val shootId : String,
        @SerializedName("photoUrl")
        val photoUrl : String,
        @SerializedName("photoName")
        val photoName : String,
        @SerializedName("photoType")
        val photoType : String
)