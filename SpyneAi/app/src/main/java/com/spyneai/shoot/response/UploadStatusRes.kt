package com.spyneai.shoot.response

import com.google.gson.annotations.SerializedName

class UploadStatusRes(
    @SerializedName("message") val message : String,
    @SerializedName("status") val status : Int,
    @SerializedName("data") val data: Data
){
    data class Data(
        @SerializedName("message")
        val upload: Boolean,
        @SerializedName("project_id")
        val projectId: String
    )
}