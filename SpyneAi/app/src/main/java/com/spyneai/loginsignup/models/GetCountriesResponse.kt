package com.spyneai.loginsignup.models

import com.google.gson.annotations.SerializedName

data class GetCountriesResponse (
    @SerializedName("data")
    val `data`: List<Data>,
    @SerializedName("status")
    val status: Int
        )