package com.spyneai.model.channel

import com.google.gson.annotations.SerializedName

data class Data (
	@SerializedName("prodId")
	val prodId : String,
	@SerializedName("markId")
	val markId : String,
	@SerializedName("displayName")
	val displayName : String,
	@SerializedName("displayThumbnail")
	val displayThumbnail : String,
	@SerializedName("def")
	val def : Boolean
)