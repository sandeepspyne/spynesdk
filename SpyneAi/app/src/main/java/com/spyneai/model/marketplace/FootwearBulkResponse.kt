package com.spyneai.model.marketplace

import com.google.gson.annotations.SerializedName

data class FootwearBulkResponse (
     val status : Int,
     val message : String,
     val after : String,
     val before : String
        )