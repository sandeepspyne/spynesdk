package com.spyneai.threesixty.data.model

data class PreSignedVideoBody(
    val authKey : String,
    val projectId : String,
    val skuId : String,
    val category : String,
    val categoryId : String,
    val subcategory : String,
    val totalFrames : Int,
    val videoName : String,
    val backgroundId : String
)