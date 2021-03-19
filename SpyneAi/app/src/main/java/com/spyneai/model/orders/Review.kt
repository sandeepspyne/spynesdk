package com.spyneai.model.orders

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.spyneai.model.order.Sku

data class Review (
        @SerializedName("userId")
        var userId: String,
        @SerializedName("data")
        val shootId: String,
        @SerializedName("businessName")
        val businessName: String,
        @SerializedName("prodId")
        val prodId: String,
        @SerializedName("catId")
        val catId: String,
        @SerializedName("categoryName")
        val categoryName: String,
        @SerializedName("productName")
        val productName: String,
        @SerializedName("status")
        val status: String,
        @SerializedName("shootName")
        val shootName: String,
        @SerializedName("numberOfSkus")
        val numberOfSkus: Long,
        @SerializedName("shootAmount")
        val shootAmount: Long,
        @SerializedName("marketPlace")
        val marketPlace: List<MarketPlace>,
        @SerializedName("skus")
        val skus: List<Sku>,
        @SerializedName("creationDate")
        val creationDate: String,
        @SerializedName("expectedDate")
        val expectedDate: String,
        @SerializedName("submittedDate")
        val submittedDate: String,
        @SerializedName("updatedAt")
        val updatedAt: String,
        @SerializedName("deliveryDate")
        val deliveryDate: String,
        @SerializedName("skuOneDisplayThumnail")
        val skuOneDisplayThumnail: String,
        @SerializedName("skuTwoDisplayThumnail")
        val skuTwoDisplayThumnail: String,
        )