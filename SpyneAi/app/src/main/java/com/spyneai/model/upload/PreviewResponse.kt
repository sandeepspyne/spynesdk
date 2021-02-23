package com.spyneai.model.upload

data class PreviewResponse(
    val url : String,
    val category : String,
    val time_taken : Double,
    val orig_url : String
)