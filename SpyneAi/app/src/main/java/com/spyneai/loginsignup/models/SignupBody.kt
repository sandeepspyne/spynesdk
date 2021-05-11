package com.spyneai.loginsignup.models

import com.google.gson.annotations.SerializedName

data class SignupBody (
    @SerializedName("email") val email : String,
    @SerializedName("companyName") val companyName : String,
    @SerializedName("name") val name : String,
    @SerializedName("contact") val contact : String,
    @SerializedName("vendorType") val vendorType : String,
    @SerializedName("password") val password : String,
    @SerializedName("isd") val isd : Int,
    @SerializedName("website") val website : String,
    @SerializedName("product") val product : String,
    @SerializedName("businessType") val businessType : String
        )