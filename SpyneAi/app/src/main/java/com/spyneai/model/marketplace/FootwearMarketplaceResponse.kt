package com.spyneai.model.marketplace

data class FootwearMarketplaceResponse (

    val id : Int,
    val market_id : String,
    val image_url : String,
    val image_credit : Int,
    val hex_code : String,
    val marketplace_name : String,
    val custom_bg_url_1 : String,
    val custom_bg_url_2 : String,
    val sample_image_1 : String,
    val sample_image_2 : String,
    val category : String,
    val created_at : String,
    val updated_at : String

)