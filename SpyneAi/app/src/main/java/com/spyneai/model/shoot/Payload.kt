package com.spyneai.model.shoot

import com.google.gson.annotations.SerializedName

data class Payload (
	@SerializedName("data")
	val data : Data
)