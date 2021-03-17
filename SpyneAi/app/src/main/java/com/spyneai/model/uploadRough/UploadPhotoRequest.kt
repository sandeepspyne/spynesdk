package com.spyneai.model.uploadRough

data class UploadPhotoRequest (
        val skuName : String,
        val skuId : String,
        val type : String,
        val indexNumber : Int,
        val shootId : String,
        val photoUrl : String,
        val photoType : String,
        val photoName : String
)