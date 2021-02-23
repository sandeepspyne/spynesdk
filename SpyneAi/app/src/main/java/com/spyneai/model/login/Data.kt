package com.spyneai.model.login
data class Data (

	val id : Int,
	val createdAt : String,
	val updatedAt : String,
	val businessName : String,
	val mobileNumber : String,
	val token : String,
	val otp : String,
	val isValidated : Boolean
)