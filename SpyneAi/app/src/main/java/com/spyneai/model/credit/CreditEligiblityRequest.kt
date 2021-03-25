package com.spyneai.model.credit

import com.google.gson.annotations.SerializedName

data class CreditEligiblityRequest (
    @SerializedName("user_id")
    val user_id : String,
    @SerializedName("email_id")
    val email_id : String
        )