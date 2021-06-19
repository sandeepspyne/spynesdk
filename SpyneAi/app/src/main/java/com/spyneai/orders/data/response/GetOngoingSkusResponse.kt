package com.spyneai.orders.data.response

data class GetOngoingSkusResponse(
    val count: Int,
    val `data`: List<Data>,
    val message: String,
    val status: Int
){
    data class Data(
        val category: String,
        val created_date: String,
        val sku_id: String,
        val sku_name: String,
        val source: String,
        val status: String,
        val sub_category: String,
        val thumbnail: Any,
        val total_exterior: Int,
        val total_focus: Any,
        val total_images: Int,
        val total_interior: Any,
        val total_processed: Any
    )
}

