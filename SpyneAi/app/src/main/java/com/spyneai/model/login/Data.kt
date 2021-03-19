package com.spyneai.model.login

import com.google.gson.annotations.SerializedName

data class Data (
	@SerializedName("id")
	val id : Int,
	@SerializedName("createdAt")
	val createdAt : String,
	@SerializedName("updatedAt")
	val updatedAt : String,
	@SerializedName("businessName")
	val businessName : String,
	@SerializedName("mobileNumber")
	val mobileNumber : String,
	@SerializedName("token")
	val token : String,
	@SerializedName("otp")
	val otp : String,
	@SerializedName("isValidated")
	val isValidated : Boolean
)