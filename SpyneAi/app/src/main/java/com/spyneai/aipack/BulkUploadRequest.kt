package com.spyneai.aipack

data class BulkUploadRequest(
    val background : List<String>,
    val image_url : List<String>,
    val user_id : String,
    val sku_id : String,
    val sku_name : String
)