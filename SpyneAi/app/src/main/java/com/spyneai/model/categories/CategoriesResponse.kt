package com.spyneai.model.categories

import com.google.gson.annotations.SerializedName
import com.spyneai.model.login.Header
import com.spyneai.model.login.MsgInfo
import com.spyneai.model.categories.Payload

data class CategoriesResponse (
    @SerializedName("header")
    val header : Header,
    @SerializedName("msgInfo")
    val msgInfo : MsgInfo,
    @SerializedName("payload")
    val payload : Payload
)