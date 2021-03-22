package com.spyneai.aipack

data class BulkUploadResponse(
    val message : String,
    val output_image : String,
    val status : Int,
    val time_taken : Double,
    val watermark_image : String
)
