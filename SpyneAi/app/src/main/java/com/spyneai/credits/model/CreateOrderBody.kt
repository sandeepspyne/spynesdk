package com.spyneai.credits.model

data class CreateOrderBody(
    val active: Boolean,
    val currency: String,
    val orderId: String,
    val planDiscount: Int,
    val planFinalCost: Float,
    val planId: String,
    val planOrigCost: Float,
    val status: String,
    val subscriptionType: String,
    val userId: String
)