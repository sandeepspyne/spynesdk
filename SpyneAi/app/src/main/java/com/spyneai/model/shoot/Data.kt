package com.spyneai.model.shoot

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Data (
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
	val shootAmount: Double,
	val marketPlace: List<Any>,
	val skus: List<Any>,
	val creationDate: String,
	val expectedDate: Any,
	val submittedDate: Any,
	val updatedAt: String,
	val deliveryDate: Any
)