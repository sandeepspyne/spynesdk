package com.spyneai.aipack

data class FetchBulkResponse (
        val id: Int,
        val user_id: String,
        val sku_id: String,
        val input_image_url: String,
        val output_image_url: String,
        val background_id: String,
        val sku_name : String,
        val created_at : String,
        val category : String,
        val product_category : String,
        val watermark_image : String
)
