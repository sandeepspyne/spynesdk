package com.spyneai.model.otp

import com.google.gson.annotations.SerializedName

data class OtpRequest (
	@SerializedName("otp")
	val otp : String
)