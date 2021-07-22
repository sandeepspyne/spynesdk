package com.spyneai.credits.model

data class CreditPlansResItem(
    val creditId: String,
    val credits: Int,
    val planType: String,
    val price: Float,
    val pricePerImage: Double,
    val rackPrice: Float,
    val rackPricePerImage: Double,
    var isSelected : Boolean = false,
    var planId : Int = 0,
    var finalPrice : Int = 0
)