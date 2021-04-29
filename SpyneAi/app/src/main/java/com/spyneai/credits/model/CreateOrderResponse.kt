package com.spyneai.credits.model

data class CreateOrderResponse(
    val active: Boolean,
    val createdAt: String,
    val currency: String,
    val id: Int,
    val orderId: String,
    val planDiscount: Double,
    val planFinalCost: Double,
    val planId: String,
    val planOrigCost: Double,
    val razorpayOrderId: String,
    val razorpayPaymentId: Any,
    val status: String,
    val subscriptionType: String,
    val updatedAt: String,
    val userId: String
)