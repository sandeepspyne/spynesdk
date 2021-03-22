package com.spyneai.model.channel

import com.google.gson.annotations.SerializedName

data class Payload (
	@SerializedName("data")
	val data : List<Data>
)