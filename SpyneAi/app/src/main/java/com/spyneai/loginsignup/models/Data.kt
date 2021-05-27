package com.spyneai.loginsignup.models

import com.google.gson.annotations.SerializedName

data class Data (
    @SerializedName("id")
    val id: Int,
    @SerializedName("isd_code")
    val isd_code: Any,
    @SerializedName("name")
    val name: String,
    @SerializedName("num_length")
    val num_length: Any
        )