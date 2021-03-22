package com.spyneai.model.ai

import com.google.gson.annotations.SerializedName

data class SendEmailRequest (
        @SerializedName("beforeList")
        val beforeList : ArrayList<String>,
        @SerializedName("afterList")
        val afterList : ArrayList<String>,
        @SerializedName("interiorList")
        val interiorList : ArrayList<String>,
        @SerializedName("imageGif")
        val imageGif : String,
        @SerializedName("emailId")
        val emailId : String
)