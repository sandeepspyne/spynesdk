package com.spyneai.model.marketplace

data class FootwearMarketplaceResponse (
    val market_id : String,
    val marketplace_name : String,
    val category : String,
    val image_credit : Int,
    val hex_code : String,
    val backgrounds : List<Backgrounds>
)