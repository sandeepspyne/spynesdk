package com.spyneai.credits.model

data class CreditPlansResItem(
    val creditId: String,
    val credits: Int,
    val planType: String,
    val price: Int,
    val pricePerImage: Double,
    val rackPrice: Int,
    val rackPricePerImage: Double,
    var isSelected : Boolean = false
)