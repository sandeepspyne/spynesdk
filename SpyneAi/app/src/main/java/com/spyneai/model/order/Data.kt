package com.spyneai.model.order

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Data(

        val userId: String,
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
)
