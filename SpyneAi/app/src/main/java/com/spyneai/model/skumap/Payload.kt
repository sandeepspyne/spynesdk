package com.spyneai.model.skumap

import com.google.gson.annotations.SerializedName

data class Payload (

	@SerializedName("data")
	val data : Data
)