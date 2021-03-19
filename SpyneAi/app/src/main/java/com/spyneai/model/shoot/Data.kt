package com.spyneai.model.shoot

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Data (
	@SerializedName("userId")
	var userId: String,
	@SerializedName("shootId")
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
	val shootAmount: Double,
	@SerializedName("marketPlace")
	val marketPlace: List<Any>,
	@SerializedName("payload")
	val skus: List<Any>,
	@SerializedName("creationDate")
	val creationDate: String,
	@SerializedName("expectedDate")
	val expectedDate: Any,
	@SerializedName("submittedDate")
	val submittedDate: Any,
	@SerializedName("updatedAt")
	val updatedAt: String,
	@SerializedName("deliveryDate")
	val deliveryDate: Any
)