package com.spyneai.model.orders

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.spyneai.model.order.Sku

data class Review (
        var userId: String,
        val shootId: String,

        val businessName: String,

        val prodId: String,

        val catId: String,

        val categoryName: String,

        val productName: String,

        val status: String,

        val shootName: String,

        val numberOfSkus: Long,

        val shootAmount: Long,

        val marketPlace: List<MarketPlace>,

        val skus: List<Sku>,

        val creationDate: String,

        val expectedDate: String,

        val submittedDate: String,

        val updatedAt: String,

        val deliveryDate: String,


        val skuOneDisplayThumnail: String,
        val skuTwoDisplayThumnail: String,
        )