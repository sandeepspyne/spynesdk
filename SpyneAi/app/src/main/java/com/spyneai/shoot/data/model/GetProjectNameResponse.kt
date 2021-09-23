package com.spyneai.shoot.data.model

import com.google.gson.annotations.SerializedName

data class GetProjectNameResponse (
    @SerializedName("status") val status : Int,
    @SerializedName("message") val message : String,
    @SerializedName("data") val data : Data
        )
{
    data class Data (

        @SerializedName("total_sku") val total_sku : Int,
        @SerializedName("total_project") val total_project : Int,
        @SerializedName("dafault_sku") val dafault_sku : String,
        @SerializedName("dafault_project") val dafault_project : String
    )
}