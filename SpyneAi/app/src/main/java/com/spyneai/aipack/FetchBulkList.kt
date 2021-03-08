package com.spyneai.aipack

data class FetchBulkList (
    val id: Int,
    val user_id: String,
    val sku_id: String,
    val input_image_url: String,
    val output_image_url: String,
    val background_id: Int
    )