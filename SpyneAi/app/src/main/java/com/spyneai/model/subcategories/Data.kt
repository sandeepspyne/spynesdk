package com.spyneai.model.subcategories

import com.google.gson.annotations.SerializedName

data class Data (
	@SerializedName("catId")
	val catId : String,
	@SerializedName("prodId")
	val prodId : String,
	@SerializedName("displayName")
	val displayName : String,
	@SerializedName("displayThumbnail")
	val displayThumbnail : String,
	@SerializedName("active")
	val active : String
)