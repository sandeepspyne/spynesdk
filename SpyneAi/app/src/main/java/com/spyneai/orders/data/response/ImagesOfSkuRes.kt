package com.spyneai.orders.data.response

data class ImagesOfSkuRes(
    val `data`: List<Data>,
    val message: String,
    val paid: String,
    val sku_status: String,
    val staus: Int
){
    data class Data(
        val background_id: String,
        val created_on: String,
        val enterprise_id: String,
        val frame_seq_no: String,
        val id: Int,
        val image_category: String,
        val image_id: String,
        val image_name: String,
        val input_image_hres_url: String,
        val input_image_lres_url: String,
        val output_image_hres_url: String,
        val output_image_lres_url: String,
        val output_image_lres_wm_url: String,
        val project_id: String,
        val sku_id: String,
        val source: String,
        val status: String,
        val updated_on: String,
        val user_id: String
    )
}