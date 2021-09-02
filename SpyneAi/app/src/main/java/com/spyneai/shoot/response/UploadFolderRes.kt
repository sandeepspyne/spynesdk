package com.spyneai.shoot.response

import com.google.gson.annotations.SerializedName

class UploadFolderRes(
    @SerializedName("message") val message : String,
    @SerializedName("status") val status : Int,
    @SerializedName("data") val data: Data
){
    data class Data(
        @SerializedName("is_folder_upload")
        val isFolderUpload: Int
    )
}